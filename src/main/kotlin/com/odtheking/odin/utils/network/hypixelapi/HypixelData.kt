package com.odtheking.odin.utils.network.hypixelapi

import com.google.gson.annotations.SerializedName
import com.odtheking.odin.utils.calculateDungeonLevel
import com.odtheking.odin.utils.magicalPower
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.startsWithOneOf
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import kotlin.io.encoding.Base64
import kotlin.jvm.optionals.getOrNull

object HypixelData {

    data class PlayerInfo(
        val profileData: ProfilesData,
        val uuid: String,
        val name: String,
    ) {
        val memberData: MemberData? by lazy { profileData.profiles.find { it.selected }?.members?.get(uuid) }
    }

    data class ProfilesData(
        val cause: String? = null,
        @SerializedName("profiles")
        private val profileList: List<Profiles>? = emptyList(), // sent as null instead of empty sometimes
    ) {
        val profiles get() = profileList.orEmpty()
    }

    data class Profiles(
        val members: Map<String, MemberData> = emptyMap(),
        val selected: Boolean = false,
    )

    private val mpRegex = Regex("§7§4☠ §cRequires §5.+§c.")

    data class MemberData(
        val rift: RiftData = RiftData(),
        @SerializedName("accessory_bag_storage")
        val accessoryBagStorage: AccessoryBagStorage = AccessoryBagStorage(),
        val dungeons: DungeonsData = DungeonsData(),
        @SerializedName("nether_island_player_data")
        val crimsonIsle: CrimsonIsle = CrimsonIsle(),
        @SerializedName("player_stats")
        val playerStats: PlayerStats = PlayerStats(),
        val inventory: Inventory? = Inventory(),
    ) {
        @Transient
        private var cachedMagicalPower: Int? = null

        val magicalPower: Int get() = cachedMagicalPower ?: calculateMagicalPower().also { cachedMagicalPower = it }

        val currentTunings: Map<String, Int> get() = accessoryBagStorage.tuning.currentTunings

        val inventoryApi get() = inventory?.eChestContents?.data?.isNotEmpty() == true

        val assumedMagicalPower get() = magicalPower.takeUnless { it == 0 } ?: (currentTunings.values.sum() * 10)

        private fun calculateMagicalPower(): Int {
            val talismans = inventory?.bagContents?.get("talisman_bag")?.itemStacks ?: return 0
            val bestPerItem = HashMap<String, Int>()
            for (item in talismans) {
                if (item == null || item.lore.any { mpRegex.matches(it) }) continue
                val mp = item.magicalPower + if (item.id == "ABICASE") crimsonIsle.abiphone.activeContacts.size / 2 else 0
                val id = if (item.id.startsWithOneOf("PARTY_HAT", "BALLOON_HAT")) "PARTY_HAT" else item.id
                bestPerItem.merge(id, mp, ::maxOf)
            }
            return bestPerItem.values.sum() + if (rift.access.consumedPrism) 11 else 0
        }
    }

    data class PlayerStats(
        val kills: Map<String, Float> = emptyMap(),
    ) {
        val bloodMobKills get() =
            ((kills["watcher_summon_undead"] ?: 0f) + (kills["master_watcher_summon_undead"] ?: 0f)).toInt()
    }

    data class CrimsonIsle(
        val abiphone: Abiphone = Abiphone(),
    )

    data class Abiphone(
        @SerializedName("active_contacts")
        val activeContacts: List<String> = emptyList(),
    )

    data class RiftData(
        val access: RiftAccess = RiftAccess(),
    )

    data class RiftAccess(
        @SerializedName("consumed_prism")
        val consumedPrism: Boolean = false
    )

    data class DungeonsData(
        @SerializedName("dungeon_types")
        val dungeonTypes: DungeonTypes = DungeonTypes(),
        @SerializedName("player_classes")
        val classes: Map<String, ClassData> = emptyMap(),
        val secrets: Long = 0,
    ) {
        @Transient
        private var cachedClassLevels: List<ClassLevel>? = null

        val classLevels: List<ClassLevel>
            get() = cachedClassLevels ?: DungeonClass.entries.filter { it != DungeonClass.EMPTY }.map {
                val xp = classes[it.name.lowercase()]?.experience ?: 0.0
                ClassLevel(it, xp, calculateDungeonLevel(xp))
            }.also { cachedClassLevels = it }

        val classAverage get() = classLevels.map { it.level }.average()

        val totalClassXp get() = classLevels.sumOf { it.xp }

        val totalRuns get() = dungeonTypes.catacombs.completions + dungeonTypes.mastermode.completions

        val avrSecrets get() = if (totalRuns > 0) secrets.toDouble() / totalRuns else 0.0
    }

    data class ClassLevel(val dungeonClass: DungeonClass, val xp: Double, val level: Double)

    data class DungeonTypes(
        val catacombs: DungeonTypeData = DungeonTypeData(),
        @SerializedName("master_catacombs")
        val mastermode: DungeonTypeData = DungeonTypeData(),
    )

    data class ClassData(
        val experience: Double = 0.0
    )

    data class DungeonTypeData(
        val experience: Double = 0.0,
        @SerializedName("tier_completions")
        val tierComps: Map<String, Float> = emptyMap(),
        @SerializedName("fastest_time")
        val fastestTimes: Map<String, Float> = emptyMap(),
        @SerializedName("watcher_kills")
        val watcherKills: Map<String, Float> = emptyMap(),
        @SerializedName("fastest_time_s_plus")
        val fastestTimeSPlus: Map<String, Double> = emptyMap(),
    ) {
        @Transient
        private var cachedLevel: Double? = null
        val level: Double get() = cachedLevel ?: calculateDungeonLevel(experience).also { cachedLevel = it }
        val completions get() = (1..7).sumOf { tierComps["$it"]?.toInt() ?: 0 }
        val watcherKillCount get() = watcherKills.values.sum().toInt()
    }

    data class AccessoryBagStorage(
        val tuning: TuningData = TuningData(),
    )

    data class TuningData(
        @SerializedName("slot_0")
        val currentTunings: Map<String, Int> = emptyMap(),
    )

    data class Inventory(
        @SerializedName("ender_chest_contents")
        val eChestContents: InventoryContents = InventoryContents(),
        @SerializedName("bag_contents")
        val bagContents: Map<String, InventoryContents> = emptyMap(),
    )

    data class InventoryContents(
        val data: String = ""
    ) {
        val itemStacks: List<ItemData?> get() = with(data) {
            if (isEmpty()) return emptyList()
            val nbtCompound = NbtIo.readCompressed(Base64.decode(this).inputStream(), NbtAccounter.unlimitedHeap())
            val itemNBTList = nbtCompound.getList("i").getOrNull() ?: return emptyList()
            itemNBTList.indices.map { i ->
                val compound = itemNBTList.getCompound(i).getOrNull()?.takeIf { it.size() > 0 } ?: return@map null
                val tag = compound.get("tag")?.asCompound()?.get() ?: return@map null
                val id = tag.get("ExtraAttributes")?.asCompound()?.get()?.get("id")?.asString()?.get() ?: ""
                val display = tag.get("display")?.asCompound()?.get() ?: return@map null
                val lore = display.get("Lore")?.asList()?.get()?.mapNotNull { it.asString().getOrNull() } ?: emptyList()
                ItemData(id, lore)
            }
        }
    }

    data class ItemData(
        val id: String,
        val lore: List<String>,
    )
}
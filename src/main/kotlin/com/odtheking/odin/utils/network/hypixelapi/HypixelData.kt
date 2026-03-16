package com.odtheking.odin.utils.network.hypixelapi

import com.google.gson.annotations.SerializedName
import com.odtheking.odin.utils.capitalizeWords
import com.odtheking.odin.utils.magicalPower
import com.odtheking.odin.utils.startsWithOneOf
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.optionals.getOrNull
import kotlin.math.floor

object HypixelData {

    data class PlayerInfo(
        val profileData: ProfilesData,
        val uuid: String,
        val name: String,
    ) {
        inline val memberData get() = profileData.profiles.find { it.selected }?.members?.get(uuid)
    }

    data class ProfilesData(
        val error: String? = null,
        val cause: String? = null,
        @SerializedName("profiles")
        private val profileList: List<Profiles>? = emptyList(), // for some reason this gets sent as null instead of empty sometimes. kinda weird.
    ) {
        val profiles get() = profileList.orEmpty()

        @Transient
        val failed: String? = when {
            error != null -> error
            cause != null -> cause
            else -> null
        }
    }

    data class Profiles(
        @SerializedName("profile_id")
        val profileId: String,
        val members: Map<String, MemberData>,
        @SerializedName("game_mode")
        val gameMode: String = "normal",
        val banking: BankingData = BankingData(),
        @SerializedName("cute_name")
        val cuteName: String,
        val selected: Boolean,
    )

    val mpRegex = Regex("§7§4☠ §cRequires §5.+§c.")

    data class MemberData(
        val rift: RiftData = RiftData(),
        @SerializedName("accessory_bag_storage")
        val accessoryBagStorage: AccessoryBagStorage = AccessoryBagStorage(),
        @SerializedName("item_data")
        val miscItemData: MiscItemData = MiscItemData(),
        val currencies: CurrencyData = CurrencyData(),
        val dungeons: DungeonsData = DungeonsData(),
        @SerializedName("pets_data")
        val pets: PetsData = PetsData(),
        @SerializedName("player_id")
        val playerId: String,
        @SerializedName("nether_island_player_data")
        val crimsonIsle: CrimsonIsle = CrimsonIsle(),
        @SerializedName("player_stats")
        val playerStats: PlayerStats = PlayerStats(),
        val inventory: Inventory? = Inventory(),
        val collection: Map<String, Long> = mapOf()
    ) {
        val magicalPower get() = inventory?.bagContents?.get("talisman_bag")?.itemStacks?.mapNotNull {
            if (it == null || it.lore.any { item -> mpRegex.matches(item) }) return@mapNotNull null
            val mp = it.magicalPower + (if (it.id == "ABICASE") floor(crimsonIsle.abiphone.activeContacts.size / 2f).toInt() else 0)
            val itemId = it.id.takeUnless { it.startsWithOneOf("PARTY_HAT", "BALLOON_HAT") } ?: "PARTY_HAT"
            itemId to mp
        }?.groupBy { it.first }?.mapValues { entry ->
            entry.value.maxBy { it.second }
        }?.values?.fold(0) { acc, pair ->
            acc + pair.second
        }?.let { it + if (rift.access.consumedPrism) 11 else 0 } ?: 0

        val tunings get() = accessoryBagStorage.tuning.currentTunings.map { "${it.key.replace("_", " ").capitalizeWords()}§7: ${it.value}" }

        val inventoryApi get() = inventory?.eChestContents?.itemStacks?.isNotEmpty() == true

        val allItems get() = ((inventory?.invContents?.itemStacks ?: emptyList()) + (inventory?.eChestContents?.itemStacks ?: emptyList()) + (inventory?.backpackContents?.flatMap { it.value.itemStacks } ?: emptyList()))

        val assumedMagicalPower get() = magicalPower.takeUnless { it == 0 } ?: (accessoryBagStorage.tuning.currentTunings.values.sum() * 10)
    }

    data class PlayerStats(
        val kills: Map<String, Float> = emptyMap(),
        val deaths: Map<String, Float> = emptyMap(),
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

    data class PetsData(val pets: List<Pet> = emptyList()) {
        @Transient
        val activePet = pets.find { it.active }
    }

    data class Pet(
        val uuid: String? = null,
        val uniqueId: String? = null,
        val type: String = "",
        val exp: Double = 0.0,
        val active: Boolean = false,
        val tier: String = "",
        val heldItem: String? = null,
        val candyUsed: Int = 0,
        val skin: String? = null,
    )

    data class DungeonsData(
        @SerializedName("dungeon_types")
        val dungeonTypes: DungeonTypes = DungeonTypes(),
        @SerializedName("player_classes")
        val classes: Map<String, ClassData> = emptyMap(),
        @SerializedName("selected_dungeon_class")
        val selectedClass: String? = null,
        @SerializedName("daily_runs")
        val dailyRuns: DailyRunData = DailyRunData(),
        @SerializedName("last_dungeon_run")
        val lastDungeonRun: String? = null,
        val secrets: Long = 0,
    ) {
        inline val totalRuns get() =
            (1..7).sumOf { tier -> (dungeonTypes.catacombs.tierComps["$tier"]?.toInt() ?: 0) + (dungeonTypes.mastermode.tierComps["$tier"]?.toInt() ?: 0) }

        inline val avrSecrets get() = if (totalRuns > 0) secrets.toDouble() / totalRuns else 0.0
    }

    data class DungeonTypes(
        val catacombs: DungeonTypeData = DungeonTypeData(),
        @SerializedName("master_catacombs")
        val mastermode: DungeonTypeData = DungeonTypeData(),
    )

    data class DailyRunData(
        @SerializedName("current_day_stamp")
        val currentDayStamp: Long? = null,
        @SerializedName("completed_runs_count")
        val completedRunsCount: Long = 0,
    )

    data class ClassData(
        val experience: Double = 0.0
    )

    data class DungeonTypeData(
        @SerializedName("times_played")
        val timesPlayed: Map<String, Double>? = null,
        val experience: Double = 0.0,
        @SerializedName("tier_completions")
        val tierComps: Map<String, Float> = emptyMap(),
        @SerializedName("milestone_completions")
        val milestoneComps: Map<String, Float> = emptyMap(),
        @SerializedName("fastest_time")
        val fastestTimes: Map<String, Float> = emptyMap(),
        @SerializedName("best_score")
        val bestScore: Map<String, Float> = emptyMap(),
        @SerializedName("mobs_killed")
        val mobsKilled: Map<String, Float> = emptyMap(),
        @SerializedName("most_mobs_killed")
        val mostMobsKilled: Map<String, Float> = emptyMap(),
        @SerializedName("most_damage_berserk")
        val mostDamageBers: Map<String, Double> = emptyMap(),
        @SerializedName("most_healing")
        val mostHealing: Map<String, Double> = emptyMap(),
        @SerializedName("watcher_kills")
        val watcherKills: Map<String, Float> = emptyMap(),
        @SerializedName("highest_tier_completed")
        val highestTierComp: Int = 0,
        @SerializedName("most_damage_tank")
        val mostDamageTank: Map<String, Double> = emptyMap(),
        @SerializedName("most_damage_healer")
        val mostDamageHealer: Map<String, Double> = emptyMap(),
        @SerializedName("fastest_time_s")
        val fastestTimeS: Map<String, Double> = emptyMap(),
        @SerializedName("most_damage_mage")
        val mostDamageMage: Map<String, Double> = emptyMap(),
        @SerializedName("fastest_time_s_plus")
        val fastestTimeSPlus: Map<String, Double> = emptyMap(),
        @SerializedName("most_damage_Archer")
        val mostDamageArcher: Map<String, Double> = emptyMap(),
    )

    data class CurrencyData(
        @SerializedName("coin_purse")
        val coins: Double = 0.0,
        @SerializedName("motes_purse")
        val motes: Double = 0.0,
        val essence: Map<String, EssenceData> = emptyMap(),
    )

    data class EssenceData(
        val current: Long = 0,
    )

    data class MiscItemData(
        val soulflow: Long = 0,
        @SerializedName("favorite_arrow")
        val favoriteArrow: String? = null,
    )

    data class AccessoryBagStorage(
        val tuning: TuningData = TuningData(),
        @SerializedName("selected_power")
        val selectedPower: String? = null,
        @SerializedName("unlocked_powers")
        val unlockedPowers: List<String> = emptyList(),
        @SerializedName("bag_upgrades_purchased")
        val bagUpgrades: Int = 0,
        @SerializedName("highest_magical_power")
        val highestMP: Long = 0,
    )

    data class TuningData(
        @SerializedName("slot_0")
        val currentTunings: Map<String, Int> = emptyMap(),
        val highestUnlockedSlot: Int = 0,
    )

    data class Inventory(
        @SerializedName("inv_contents")
        val invContents: InventoryContents = InventoryContents(),
        @SerializedName("ender_chest_contents")
        val eChestContents: InventoryContents = InventoryContents(),
        @SerializedName("backpack_icons")
        val backpackIcons: Map<String, InventoryContents> = emptyMap(),
        @SerializedName("bag_contents")
        val bagContents: Map<String, InventoryContents> = emptyMap(),
        @SerializedName("inv_armor")
        val invArmor: InventoryContents = InventoryContents(),
        @SerializedName("equipment_contents")
        val equipment: InventoryContents = InventoryContents(),
        @SerializedName("wardrobe_equipped_slot")
        val wardrobeEquipped: Int? = null,
        @SerializedName("backpack_contents")
        val backpackContents: Map<String, InventoryContents> = emptyMap(),
        @SerializedName("sacks_counts")
        val sacks: Map<String, Long> = emptyMap(),
        @SerializedName("personal_vault_contents")
        val personalVault: InventoryContents = InventoryContents(),
        @SerializedName("wardrobe_contents")
        val wardrobeContents: InventoryContents = InventoryContents()
    )

    data class BankingData(
        val balance: Double = 0.0
    )

    data class InventoryContents(
        val type: Int? = null,
        val data: String = ""
    ) {
        @OptIn(ExperimentalEncodingApi::class)
        val itemStacks: List<ItemData?> get() = with(data) {
            if (isEmpty()) return emptyList()
            val nbtCompound = NbtIo.readCompressed(Base64.decode(this).inputStream(), NbtAccounter.unlimitedHeap())
            val itemNBTList = nbtCompound.getList("i").getOrNull() ?: return emptyList()
            itemNBTList.indices.map { i ->
                val compound = itemNBTList.getCompound(i).getOrNull()?.takeIf { it.size() > 0 } ?: return@map null
                val tag = compound.get("tag")?.asCompound()?.get() ?: return@map null
                val id = tag.get("ExtraAttributes")?.asCompound()?.get()?.get("id")?.asString()?.get() ?: ""
                val display = tag.get("display")?.asCompound()?.get() ?: return@map null
                val name = display.get("Name")?.asString()?.get() ?: ""
                val lore = display.get("Lore")?.asList()?.get()?.mapNotNull { it.asString().getOrNull() } ?: emptyList()
                ItemData(name, id, lore)
            }
        }
    }

    data class ItemData(
        val name: String,
        val id: String,
        val lore: List<String>,
    )
}
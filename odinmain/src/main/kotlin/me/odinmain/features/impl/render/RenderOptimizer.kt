package me.odinmain.features.impl.render

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.events.impl.ReceivePacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getSkullValue
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RenderOptimizer : Module(
    name = "Render Optimizer",
    category = Category.RENDER,
    description = "Disables certain render function when they are not necessary."
) {

    private val fallingBlocks: Boolean by BooleanSetting(name = "Remove Falling Blocks", default = true, description = "Removes falling blocks that are not necessary.")
    private val p5Mobs: Boolean by BooleanSetting(name = "Remove P5 Armor Stands", default = true, description = "Removes armor stands that are not necessary.")
    private val hideParticles: Boolean by BooleanSetting(name = "Hide P5 Particles", default = true, description = "Hides particles that are not necessary.")
    private val hideHeartParticles: Boolean by BooleanSetting(name = "Hide Heart Particles", default = true, description = "Hides heart particles.")
    private val hideHealerFairy: Boolean by BooleanSetting(name = "Hide Healer Fairy", default = true, description = "Hides the healer fairy.")
    private val hideSoulWeaver: Boolean by BooleanSetting(name = "Hide Soul Weaver", default = true, description = "Hides the soul weaver.")
    private val hideArcherBones: Boolean by BooleanSetting(name = "Hide Archer Bones", default = true, description = "Hides the archer bones.")
    private val hideWitherMinerName: Boolean by BooleanSetting(name = "Hide WitherMiner Name", default = true, description = "Hides the wither miner name.")
    private val hideTerracottaName: Boolean by BooleanSetting(name = "Hide Terracota Name", default = true, description = "Hides the terracota name.")
    private val hideNonStarredMobName: Boolean by BooleanSetting(name = "Hide Non-Starred Mob Name", default = true, description = "Hides the non-starred mob name.")
    private val removeArmorStands: Boolean by BooleanSetting(name = "Removes Useless ArmorStands", default = true, description = "Removes armor stands that are not necessary.")

    private const val TENTACLE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM3MjIzZDAxOTA2YWI2M2FmMWExNTk4ODM0M2I4NjM3ZTg1OTMwYjkwNWMzNTEyNWI1NDViMzk4YzU5ZTFjNSJ9fX0="
    private const val HEALER_FAIRY_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjM2UzMWNmYzY2NzMzMjc1YzQyZmNmYjVkOWE0NDM0MmQ2NDNiNTVjZDE0YzljNzdkMjczYTIzNTIifX19"
    private const val SOUL_WEAVER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0="

    private val dungeonMobSpawns = setOf(
        "Lurker",
        "Dreadlord",
        "Souleater",
        "Zombie",
        "Skeleton",
        "Skeletor",
        "Sniper",
        "Super Archer",
        "Spider",
        "Fels",
        "Withermancer"
    )
    @SubscribeEvent
    fun spawnObject(event: ReceivePacketEvent) {
        if (event.packet !is S0EPacketSpawnObject || event.packet.type != 70 || !fallingBlocks) return
        event.isCanceled = true
    }
    @SubscribeEvent
    fun postMetaData(event: PostEntityMetadata) {
        val entity = mc.theWorld.getEntityByID(event.packet.entityId) ?: return

        if (hideArcherBones) handleHideArcherBones(entity)
        if (p5Mobs) handleP5Mobs(entity)
        if (hideHealerFairy) handleHealerFairy(entity)
        if (hideSoulWeaver) handleSoulWeaver(entity)

    }
    @SubscribeEvent
    fun entityJoinWorld(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityArmorStand || !event.entity.isInvisible || removeArmorStands) return
        val inventoryList = event.entity.inventory.filterNotNull()
        if (inventoryList.size != 1 || inventoryList.first().item !is ItemBlock) return
        event.entity.setDead()
    }

    @SubscribeEvent
    fun handleNames(event: RenderLivingEvent.Pre<*>) {
        if (hideNonStarredMobName) hideNonStarredMob(event.entity)
        if (hideWitherMinerName) handleWitherMiner(event.entity)
        if (hideTerracottaName) handleTerracota(event.entity)
    }

    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S2APacketParticles) return

        if (DungeonUtils.getPhase() == 5 && hideParticles &&
            !event.packet.particleType.name.containsOneOf("ENCHANTMENT TABLE", "FLAME", "FIREWORKS_SPARK"))
            event.isCanceled = true

        if (hideHeartParticles && event.packet.particleType.name.containsOneOf("HEART"))
            event.isCanceled = true
    }

    private fun handleHideArcherBones(entity: Entity) {
        val itemEntity = entity as? EntityItem
        if (itemEntity != null && DungeonUtils.inDungeons
            && itemEntity.entityItem.itemDamage == 15
            && itemEntity.entityItem.item === Items.dye) {
            entity.setDead()
        }
    }

    private fun handleP5Mobs(entity: Entity) {
        val armorStand = entity as? EntityArmorStand
        if (DungeonUtils.getPhase() == 5 && getSkullValue(armorStand) == (TENTACLE_TEXTURE) ) {
            armorStand?.setDead()
        }
    }

    private fun handleHealerFairy(entity: Entity) {
        val armorStand = entity as? EntityArmorStand
        if (armorStand?.heldItem == null) return
        if (DungeonUtils.inDungeons && armorStand.heldItem?.item == Items.skull
            && getHealerFairyTextureValue(armorStand) == (HEALER_FAIRY_TEXTURE)) {
            armorStand.setDead()
        }
    }

    private fun handleSoulWeaver(entity: Entity) {
        val armorStand = entity as? EntityArmorStand
        if (DungeonUtils.inDungeons && getSkullValue(armorStand)?.contains(SOUL_WEAVER_TEXTURE) == true) {
            armorStand?.setDead()
        }
    }

    private fun handleWitherMiner(entity: Entity) {
        val customName = entity.customNameTag.noControlCodes
        if (entity !is EntityArmorStand || !customName.hasWitherMinerName()) return

        mc.theWorld.removeEntity(entity)
    }

    private fun handleTerracota(entity: Entity) {
        val customName = entity.customNameTag.noControlCodes
        if (customName.contains("Terracotta ")) {
            mc.theWorld.removeEntity(entity)
        }
    }

    private fun hideNonStarredMob(entity: Entity) {
        val name = entity.customNameTag
        if (!name.startsWith("§6✯ ") && name.contains("§c❤") && dungeonMobSpawns.any { it in name }) {
            mc.theWorld.removeEntity(entity)
        }
    }

    private fun String.hasWitherMinerName(): Boolean {
        return contains("Wither Miner") || contains("Wither Guard") || contains("Apostle")
    }

    private fun getHealerFairyTextureValue(armorStand: EntityArmorStand?): String? {
        return armorStand?.heldItem
            ?.tagCompound
            ?.getCompoundTag("SkullOwner")
            ?.getCompoundTag("Properties")
            ?.getTagList("textures", 10)
            ?.getCompoundTagAt(0)
            ?.getString("Value")
    }


}
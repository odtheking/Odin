package me.odinmain.features.impl.render

import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.getSkullValue
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.init.Items
import net.minecraft.network.play.server.*
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RenderOptimizer : Module(
    name = "Render Optimizer",
    category = Category.RENDER,
    description = "Disables certain render function when they are not necessary."
) {
    private val fallingBlocks by BooleanSetting(name = "Remove Falling Blocks", default = true, description = "Removes falling blocks that are not necessary.")
    private val removeTentacles by BooleanSetting(name = "Remove P5 Tentacles", default = true, description = "Removes armorstands of tentacles which are not necessary.")
    private val hideHealerFairy by BooleanSetting(name = "Hide Healer Fairy", default = true, description = "Hides the healer fairy.")
    private val hideSoulWeaver by BooleanSetting(name = "Hide Soul Weaver", default = true, description = "Hides the soul weaver.")
    private val hideArcherBones by BooleanSetting(name = "Hide Archer Bones", default = true, description = "Hides the archer bones.")
    private val hide0HealthNames by BooleanSetting(name = "Hide 0 Health", default = true, description = "Hides the names of entities with 0 health.")
    private val hideWitherMinerName by BooleanSetting(name = "Hide WitherMiner Name", default = true, description = "Hides the wither miner name.")
    private val hideTerracottaName by BooleanSetting(name = "Hide Terracota Name", default = true, description = "Hides the terracota name.")
    private val hideNonStarredMobName by BooleanSetting(name = "Hide Non-Starred Mob Name", default = true, description = "Hides the non-starred mob name.")
    private val removeBlazePuzzleNames by BooleanSetting(name = "Hide blazes", default = false, description = "Hides the blazes in the blaze puzzle room.")

    private val showParticleOptions by DropdownSetting("Show Particles Options")
    private val removeExplosion by BooleanSetting("Remove Explosion", default = false, description = "Removes explosion particles.").withDependency { showParticleOptions }
    private val hideParticles by BooleanSetting(name = "Hide P5 Particles", default = true, description = "Hides particles that are not necessary.").withDependency { showParticleOptions }
    private val hideHeartParticles by BooleanSetting(name = "Hide Heart Particles", default = false, description = "Hides heart particles.").withDependency { showParticleOptions }

    private const val TENTACLE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM3MjIzZDAxOTA2YWI2M2FmMWExNTk4ODM0M2I4NjM3ZTg1OTMwYjkwNWMzNTEyNWI1NDViMzk4YzU5ZTFjNSJ9fX0="
    private const val HEALER_FAIRY_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjM2UzMWNmYzY2NzMzMjc1YzQyZmNmYjVkOWE0NDM0MmQ2NDNiNTVjZDE0YzljNzdkMjczYTIzNTIifX19"
    private const val SOUL_WEAVER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0="

    private val dungeonMobSpawns = setOf("Lurker", "Dreadlord", "Souleater", "Zombie", "Skeleton", "Skeletor", "Sniper", "Super Archer", "Spider", "Fels", "Withermancer")

    init {
        execute(500) {
            mc.theWorld?.loadedEntityList?.forEach {
                if (!DungeonUtils.inDungeons) return@execute
                if (removeBlazePuzzleNames) removeBlazePuzzleNames(it)
                if (it !is EntityArmorStand) return@forEach
                if (hideArcherBones) handleHideArcherBones(it)
                if (removeTentacles) removeTentacles(it)
                if (hideHealerFairy) handleHealerFairy(it)
                if (hideSoulWeaver) handleSoulWeaver(it)
                if (hideNonStarredMobName) hideNonStarredMob(it)
                if (hideWitherMinerName) handleWitherMiner(it)
                if (hideTerracottaName) handleTerracotta(it)
            }
        }
    }

    private val healthMatches = arrayOf(
        Regex("^§.\\[§.Lv\\d+§.] §.+ (?:§.)+0§f/.+§c❤$"),
        Regex("^.+ (?:§.)+0§c❤$")
    )

    @SubscribeEvent
    fun onPacket(event: PacketReceivedEvent) {
        if (!LocationUtils.inSkyblock) return
        if (event.packet is S1CPacketEntityMetadata && hide0HealthNames) {
            val entity = mc.theWorld?.getEntityByID(event.packet.entityId) ?: return
            val list = event.packet.func_149376_c() ?: return

            list.filterIsInstance<String>()
                .takeUnless { strings -> strings.any { healthMatches.any { regex -> regex.matches(it) } } }
                ?.forEach { _ -> entity.alwaysRenderNameTag = false }
        }

        if (event.packet is S0EPacketSpawnObject && event.packet.type == 70 && fallingBlocks) event.isCanceled = true

        if (event.packet !is S2APacketParticles) return

        if (event.packet.particleType.equalsOneOf(EnumParticleTypes.EXPLOSION_NORMAL, EnumParticleTypes.EXPLOSION_LARGE, EnumParticleTypes.EXPLOSION_HUGE) && removeExplosion)
            event.isCanceled = true


        if (DungeonUtils.getF7Phase() == M7Phases.P5 && hideParticles && !event.packet.particleType.equalsOneOf(EnumParticleTypes.ENCHANTMENT_TABLE, EnumParticleTypes.FLAME, EnumParticleTypes.FIREWORKS_SPARK))
            event.isCanceled = true

        if (hideHeartParticles && event.packet.particleType == EnumParticleTypes.HEART)
            event.isCanceled = true
    }

    private fun handleHideArcherBones(entity: Entity) {
        val itemEntity = entity as? EntityItem ?: return
        if (DungeonUtils.inDungeons && itemEntity.entityItem.itemDamage == 15 && itemEntity.entityItem.item === Items.dye)
            entity.setDead()
    }

    private fun removeTentacles(entity: Entity) {
        if (DungeonUtils.getF7Phase() == M7Phases.P5 && getSkullValue(entity) == TENTACLE_TEXTURE)
            entity.setDead()
    }

    private fun handleHealerFairy(entity: Entity) {
        val armorStand = entity as? EntityArmorStand ?: return
        if (DungeonUtils.inDungeons && armorStand.heldItem?.item == Items.skull && getHealerFairyTextureValue(armorStand) == HEALER_FAIRY_TEXTURE)
            armorStand.setDead()
    }

    private fun handleSoulWeaver(entity: Entity) {
        if (DungeonUtils.inDungeons && getSkullValue(entity) == SOUL_WEAVER_TEXTURE) entity.setDead()
    }

    private fun handleWitherMiner(entity: Entity) {
        if (entity.customNameTag.noControlCodes.containsOneOf("Wither Miner", "Wither Guard", "Apostle"))
            entity.alwaysRenderNameTag = false
    }

    private fun handleTerracotta(entity: Entity) {
        if (entity.customNameTag.noControlCodes.contains("Terracotta "))
            entity.alwaysRenderNameTag = false
    }

    private fun hideNonStarredMob(entity: Entity) {
        val name = entity.customNameTag
        if (!name.startsWith("§6✯ ") && name.contains("§c❤") && dungeonMobSpawns.any { it in name })
            entity.alwaysRenderNameTag = false
    }

    private fun removeBlazePuzzleNames(entity: Entity) {
        if (entity is EntityBlaze) entity.setDead()
        val name = entity.customNameTag
        if (name.noControlCodes.startsWith("[Lv15] Blaze "))
            entity.alwaysRenderNameTag = false
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
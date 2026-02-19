package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.EntityEvent
import com.odtheking.odin.events.ParticleAddEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.texture
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

// I would rename to render hider (or something along those lines),
// since this module doesn't actually optimize rendering, it just hides it.
object RenderOptimizer : Module(
    name = "Render Optimizer",
    description = "Optimizes rendering by disabling unnecessary features."
) {
    private val hideFallingBlocks by BooleanSetting("Hide Falling Blocks", true, desc = "Hides rendering of falling blocks to improve performance.")
    private val hideLightning by BooleanSetting("Hide Lightning", true, desc = "Hides lightning bolts.")
    private val hideExperienceOrbs by BooleanSetting("Hide Experience Orbs", true, desc = "Hides experience orbs.")
    private val hideDeathAnimation by BooleanSetting("Hide Death Animation", true, desc = "Hides mobs that are dying.")
    private val hideDyingMobsArmorStand by BooleanSetting("Hide Armor Stands", false, desc = "Hides Armor stands from mobs that are dying.").withDependency { hideDeathAnimation }
    private val disableExplosion by BooleanSetting("Hide Explosion Particles", false, desc = "Hides explosion particles to improve performance.")
    private val hideArcherBoneMeal by BooleanSetting("Hide Archer Passive", true, desc = "Hides the archer passive's floating bone meal.")
    private val hideFairy by BooleanSetting("Hide Healer Fairy", true, desc = "Hides the healer fairy held by some mobs.")
    private val hideWeaver by BooleanSetting("Hide Soul Weaver", true, desc = "Hides the soul weaver helmet worn by some mobs.")
    private val hideTentacle by BooleanSetting("Hide Tentacle Head", true, desc = "Hides the tentacle head worn by some mobs.")

    private val disableFireOverlay by BooleanSetting("Hide Fire Overlay", true, desc = "Hides the fire overlay to improve disability.")

    private const val TENTACLE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1NzI3NzI0OSwKICAicHJvZmlsZUlkIiA6ICIxODA1Y2E2MmM0ZDI0M2NiOWQxYmY4YmM5N2E1YjgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSdWxsZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdkODM2NzQ5MjZiODk3MTRlNmI1YTU1NDcwNTAxYzA0YjA2NmRkODdiZjZjMzM1Y2RkYzZlNjBhMWExYTVmNSIKICAgIH0KICB9Cn0="
    private const val HEALER_FAIRY_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTQ2MzA5MTA0NywKICAicHJvZmlsZUlkIiA6ICIyNjRkYzBlYjVlZGI0ZmI3OTgxNWIyZGY1NGY0OTgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJxdWludHVwbGV0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJlZWRjZmZjNmExMWEzODM0YTI4ODQ5Y2MzMTZhZjdhMjc1MmEzNzZkNTM2Y2Y4NDAzOWNmNzkxMDhiMTY3YWUiCiAgICB9CiAgfQp9"
    private const val SOUL_WEAVER_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NTk1ODAzNjI1NTMsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZjI0ZWQ2ODc1MzA0ZmE0YTFmMGM3ODViMmNiNmE2YTcyNTYzZTlmM2UyNGVhNTVlMTgxNzg0NTIxMTlhYTY2In19fQ=="

    init {
        on<EntityEvent.Add> {
            when (entity.type) {
                EntityType.FALLING_BLOCK if hideFallingBlocks -> cancel()
                EntityType.LIGHTNING_BOLT if hideLightning -> cancel()
                EntityType.EXPERIENCE_ORB if hideExperienceOrbs -> cancel()
            }
        }

        on<EntityEvent.SetData> {
            if (hideArcherBoneMeal && DungeonUtils.inDungeons) {
                val item = synchedDataValues.find { it.id == 8 }?.value as? ItemStack ?: return@on
                if (!item.isEmpty && item.item == Items.BONE_MEAL) mc.execute {
                        mc.level?.removeEntity(entity.id, Entity.RemovalReason.DISCARDED)
                    }
            }
        }

        on<ParticleAddEvent> {
            if (disableExplosion && particle == ParticleTypes.EXPLOSION || particle == ParticleTypes.EXPLOSION_EMITTER)
                cancel()
        }

        on<EntityEvent.SetItemSlot> {
            if (!DungeonUtils.inDungeons) return@on

            if (stack.isEmpty) return@on
            val texture = stack.texture ?: return@on

            if (
                (hideFairy && slot == EquipmentSlot.MAINHAND && texture == HEALER_FAIRY_TEXTURE) ||
                (hideWeaver && slot == EquipmentSlot.HEAD && texture == SOUL_WEAVER_TEXTURE) ||
                (hideTentacle && slot == EquipmentSlot.HEAD && texture == TENTACLE_TEXTURE)
            ) mc.execute { mc.level?.removeEntity(entity.id, Entity.RemovalReason.DISCARDED) }

        }
    }

    /**
     * @see com.odtheking.mixin.mixins.ScreenEffectRendererMixin.onRenderFireOverlay
     */
    @JvmStatic
    fun shouldDisableFireOverlay(): Boolean {
        return enabled && disableFireOverlay
    }

    /**
     * @see com.odtheking.mixin.mixins.EntityRendererMixin.onRender
     */
    @JvmStatic
    fun hideEntityDeathAnimation(): Boolean {
        return enabled && hideDeathAnimation
    }

    /**
     * @see com.odtheking.mixin.mixins.EntityRendererMixin.onRender
     */
    @JvmStatic
    fun hideDyingEntityArmorStand(): Boolean {
        // no need for enabled, this cannot be called if hideEntityDeathAnimation returned false
        return enabled && hideDyingMobsArmorStand
    }
}
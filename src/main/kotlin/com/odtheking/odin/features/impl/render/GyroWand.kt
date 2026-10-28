package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.ParticleEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.render.drawCylinder
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getAbilityCooldown
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.Items
import kotlin.math.abs

object GyroWand : Module(
    name = "Gyro Wand",
    description = "Shows area of effect and cooldown of the Gyrokinetic Wand."
) {
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_PURPLE.withAlpha(0.5f), true, desc = "The color of the Gyrokinetic Wand range.")
    private val showCooldown by BooleanSetting("Show Cooldown", true, desc = "Shows the cooldown of the Gyrokinetic Wand.")
    private val cooldownColor by ColorSetting("Cooldown Color", Colors.MINECRAFT_RED.withAlpha(0.5f), true, desc = "The color of the cooldown of the Gyrokinetic Wand.").withDependency { showCooldown }
    private val depthCheck by BooleanSetting("Depth Check", true, desc = "Whether or not the cylinder should have a depth check.")

    private data class Burst(val start: Long = 0L, val y: Double = 0.0, val count: Int = 0)

    private var burst = Burst()
    private var lastUse = 0L

    private val onCooldown get() =
        System.currentTimeMillis() - lastUse < getAbilityCooldown(30_000L)

    init {
        on<ParticleEvent> {
            if (particle != ParticleTypes.WITCH || xd != 0.0 || yd != 0.0 || zd != 0.0 || onCooldown) return@on
            val now = System.currentTimeMillis()

            burst = if (now - burst.start > 120L || abs(y - burst.y) > 0.02) Burst(now, y, 1)
            else burst.copy(count = burst.count + 1)

            if (burst.count >= 5) {
                lastUse = now
                burst = Burst()
            }
        }

        on<RenderExtractEvent> {
            val player = mc.player ?: return@on
            player.mainHandItem.takeIf { it.item == Items.BLAZE_ROD && it.itemId == "GYROKINETIC_WAND" } ?: return@on
            val position = Etherwarp.getEtherPos(player.position(), distance = 25.0, etherWarp = false).takeIf { it.state?.isAir == false } ?: return@on

            drawCylinder(
                position.vec3.add(0.5, 1.0, 0.5), 10f, 0.3f,
                if (showCooldown && onCooldown) cooldownColor else color,
                64, depth = depthCheck
            )
        }

        on<LevelEvent.Load> {
            lastUse = 0L
            burst = Burst()
        }
    }
}
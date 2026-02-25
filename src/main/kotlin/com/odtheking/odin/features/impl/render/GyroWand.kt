package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.render.drawCylinder
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getAbilityCooldown
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.world.item.Items

object GyroWand : Module(
    name = "Gyro Wand",
    description = "Shows area of effect and cooldown of the Gyrokinetic Wand."
) {
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_PURPLE.withAlpha(0.5f), allowAlpha = true, desc = "The color of the Gyrokinetic Wand range.")
    private val showCooldown by BooleanSetting("Show Cooldown", true, desc = "Shows the cooldown of the Gyrokinetic Wand.")
    private val cooldownColor by ColorSetting("Cooldown Color", Colors.MINECRAFT_RED.withAlpha(0.5f), allowAlpha = true, desc = "The color of the cooldown of the Gyrokinetic Wand.").withDependency { showCooldown }
    private val depthCheck by BooleanSetting("Depth Check", true, desc = "Whether or not the cylinder should have a depth check.")

    private val gravityStormRegex = Regex("(?s)(.*(-\\d+ Mana \\(Gravity Storm\\)).*)")
    private var cooldownTimer = 0L

    init {
        onReceive<ClientboundSystemChatPacket> {
            if (!overlay) return@onReceive
            val msg = content?.string?.noControlCodes ?: return@onReceive
            if (msg.matches(gravityStormRegex)) cooldownTimer = System.currentTimeMillis()
        }

        on<RenderEvent.Extract> {
            val mainHand = mc.player?.mainHandItem ?: return@on
            if (mainHand.item != Items.BLAZE_ROD || mainHand.itemId != "GYROKINETIC_WAND") return@on
            val position = Etherwarp.getEtherPos(mc.player?.position(), distance = 25.0, etherWarp = false).takeIf { it.state?.isAir == false }?: return@on
            drawCylinder(
                position.vec3.add(0.5, 1.0, 0.5), 10f, 0.3f,
                if (showCooldown && System.currentTimeMillis() - cooldownTimer < getAbilityCooldown(30_000L)) cooldownColor else color, 64, depth = depthCheck
            )
        }

        on<WorldEvent.Load> {
            cooldownTimer = 0L
        }
    }
}
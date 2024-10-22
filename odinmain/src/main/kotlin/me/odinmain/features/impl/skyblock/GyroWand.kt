package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.toVec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GyroWand : Module(
    name = "Gyro Wand",
    description = "Helpful features for the Gyrokinetic Wand.",
    category = Category.SKYBLOCK
) {
    private val color by ColorSetting("Color", Color.MAGENTA.withAlpha(0.5f), allowAlpha = true, description = "The color of the Gyrokinetic Wand range.")
    private val thickness by NumberSetting("Thickness", 0.4f, 0, 10, 0.05f, description = "The thickness of the Gyrokinetic Wand range.")
    private val steps by NumberSetting("Smoothness", 40, 20, 80, 1, description = "The amount of steps to use when rendering the Gyrokinetic Wand range.")
    private val showCooldown by BooleanSetting("Show Cooldown", true, description = "Shows the cooldown of the Gyrokinetic Wand.")
    private val cooldownColor by ColorSetting("Cooldown Color", Color.RED, allowAlpha = true, description = "The color of the cooldown of the Gyrokinetic Wand.").withDependency { showCooldown }

    private val gyroCooldown = Clock(30_000)

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (heldItem?.itemID != "GYROKINETIC_WAND") return
        val position = EtherWarpHelper.getEtherPos(distance = 25.0).pos?.takeIf { !getBlockAt(it).isAir(mc.theWorld, it) }?.toVec3() ?: return

        Renderer.drawCylinder(
            position.addVector(0.5, 1.0, 0.5),
            10f, 10f - thickness, 0.2f,
            steps, 1, 0f, 90f, 90f, if (showCooldown && !gyroCooldown.hasTimePassed()) cooldownColor else color
        )
    }

    init {
        onMessage(Regex("(?s)(.*(-\\d+ Mana \\(Gravity Storm\\)).*)")) {
            gyroCooldown.update()
        }
    }
}

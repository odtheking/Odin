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
    description = "Helpful features for the Gyrokinetic Wand",
    category = Category.SKYBLOCK
) {
    private val color: Color by ColorSetting("Color", Color.MAGENTA.withAlpha(0.5f), allowAlpha = true)
    private val thickness: Float by NumberSetting("Thickness", 0.4f, 0, 10, 0.05)
    private val steps: Int by NumberSetting("Smoothness", 40, 20, 80, 1)
    private val showCooldown: Boolean by BooleanSetting("Show Cooldown", true, description = "Shows the cooldown of the Gyrokinetic Wand.")
    private val cooldownColor: Color by ColorSetting("Cooldown Color", Color.RED, allowAlpha = true).withDependency { showCooldown }

    private val gyroCooldown = Clock(30_000)

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (heldItem?.itemID != "GYROKINETIC_WAND") return
        val position = mc.thePlayer?.rayTrace(25.0, event.partialTicks)?.blockPos?.takeIf { !getBlockAt(it).isAir(mc.theWorld, it) }?.toVec3() ?: return

        val finalColor = if (showCooldown && !gyroCooldown.hasTimePassed()) cooldownColor else color

        Renderer.drawCylinder(
            position.addVector(0.5, 1.0, 0.5),
            10f, 10f - thickness, 0.2f,
            steps, 1, 0f, 90f, 90f, finalColor
        )
    }

    init {
        onMessage(Regex("(?s)(.*(-\\d+ Mana \\(Gravity Storm\\)).*)")) {
            gyroCooldown.update()
        }
    }
}

package me.odinmain.features.impl.render

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.features.Module
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.drawStringWidth

object PerformanceHUD : Module(
    name = "Performance HUD",
    description = "Displays your current ping, FPS and server's TPS."
) {
    private val nameColor by ColorSetting("Name Color", Color(50, 150, 220), desc = "The color of the stat information.")
    private val valueColor by ColorSetting("Value Color", Colors.WHITE, desc = "The color of the stat values.")
    private val direction by SelectorSetting("Direction", "Vertical", listOf("Horizontal", "Vertical"), "Direction the information is placed or something.")
    private val showFPS by BooleanSetting("Show FPS", true, desc = "Shows the FPS in the HUD.")
    private val showTPS by BooleanSetting("Show TPS", true, desc = "Shows the TPS in the HUD.")
    private val showPing by BooleanSetting("Show Ping", true, desc = "Shows the ping in the HUD.")

    private const val HORIZONTAL = 0

    private val hud by HUD("Performance HUD", "Shows performance information on the screen.") {
        if (!showFPS && !showTPS && !showPing) return@HUD 0f to 0f
        // if direction is vertical, this is max width otherwise use as current_x
        var width = 1f
        var height = 1f

        if (showFPS) {
            val x = if (direction == HORIZONTAL) width else 1f
            val w = drawText("FPS: ", "${mc.debug?.split(" ")?.get(0)?.toIntOrNull() ?: 0} ", x, height)
            if (direction == HORIZONTAL) {
                width += w
            } else {
                width = maxOf(width, w)
                height += mc.fontRendererObj.FONT_HEIGHT
            }
        }
        if (showTPS) {
            val x = if (direction == HORIZONTAL) width else 1f
            val w = drawText("TPS: ", "${ServerUtils.averageTps.toInt()} ", x, height)
            if (direction == HORIZONTAL) {
                width += w
            } else {
                width = maxOf(width, w)
                height += mc.fontRendererObj.FONT_HEIGHT
            }
        }
        if (showPing) {
            val x = if (direction == HORIZONTAL) width else 1f
            val w = drawText("Ping: ", "${ServerUtils.averagePing.toInt()}ms ", x, height)
            if (direction == HORIZONTAL) {
                width += w
            } else {
                width = maxOf(width, w)
                height += mc.fontRendererObj.FONT_HEIGHT
            }
        }
        if (direction == HORIZONTAL) {
            height = mc.fontRendererObj.FONT_HEIGHT.toFloat()
        }
        width to height
    }

    private fun drawText(name: String, value: String, x: Float, y: Float): Float {
        val x1 = if (direction == HORIZONTAL) x else 1f
        var width = 0f
        width += drawStringWidth(name, x1 + width, y, nameColor, true)
        width += drawStringWidth(value, x1 + width, y, valueColor, true)
        return width
    }
}
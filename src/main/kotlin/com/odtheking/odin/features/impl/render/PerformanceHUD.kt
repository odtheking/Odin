package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.toFixed
import net.minecraft.client.gui.GuiGraphicsExtractor

object PerformanceHUD : Module(
    name = "Performance HUD",
    description = "Shows performance information on the screen."
) {
    private val nameColor by ColorSetting("Name Color", Color(50, 150, 220), desc = "The color of the stat information.")
    private val valueColor by ColorSetting("Value Color", Colors.WHITE, desc = "The color of the stat values.")
    private val direction by SelectorSetting("Direction", "Horizontal", listOf("Horizontal", "Vertical"), "Direction the information is displayed.")
    private val showFPS by BooleanSetting("Show FPS", true, desc = "Shows the FPS in the HUD.")
    private val showTPS by BooleanSetting("Show TPS", true, desc = "Shows the TPS in the HUD.")
    private val showPing by BooleanSetting("Show Ping", true, desc = "Shows the ping in the HUD.")

    private const val HORIZONTAL = 0

    private val hud by HUD(name, "Shows performance information on the screen.", false) {
        if (!showFPS && !showTPS && !showPing) return@HUD 0 to 0

        var width = 1
        var height = 1
        val lineHeight = mc.font.lineHeight

        fun renderMetric(label: String, value: String) {
            val w = drawText(label, value, if (direction == HORIZONTAL) width else 1, height)
            if (direction == HORIZONTAL) width += w
            else {
                width = maxOf(width, w)
                height += lineHeight
            }
        }

        if (showTPS) renderMetric("TPS: ", "${ServerUtils.averageTps.toFixed(1)} ")
        if (showFPS) renderMetric("FPS: ", "${mc.fps} ")
        if (showPing) renderMetric("Ping: ", "${ServerUtils.averagePing}ms ")

        width to if (direction == HORIZONTAL) lineHeight else height
    }

    private fun GuiGraphicsExtractor.drawText(name: String, value: String, x: Int, y: Int): Int {
        var width = 0
        width += textDim(name, x, y, nameColor, true).first
        width += textDim(value, x + width, y, valueColor, true).first
        return width
    }
}
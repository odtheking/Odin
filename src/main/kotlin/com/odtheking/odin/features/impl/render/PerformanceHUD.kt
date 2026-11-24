package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ServerUtils
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.toFixed
import net.minecraft.client.gui.GuiGraphicsExtractor

object PerformanceHUD : Module(
    name = "Performance HUD",
    description = "Shows performance information on the screen."
) {
    private val nameColor by ColorSetting("Name Color", Color(50, 150, 220), desc = "The color of the stat information.")
    private val valueColor by ColorSetting("Value Color", Colors.WHITE, desc = "The color of the stat values.")
    private val direction by SelectorSetting("Direction", Direction.HORIZONTAL, "Direction the information is displayed.")

    enum class Direction { HORIZONTAL, VERTICAL }
    private val showFPS by BooleanSetting("Show FPS", true, desc = "Shows the FPS in the HUD.")
    private val showTPS by BooleanSetting("Show TPS", true, desc = "Shows the TPS in the HUD.")
    private val showPing by BooleanSetting("Show Ping", true, desc = "Shows the ping in the HUD.")

    private val hud by HUD(name, "Shows performance information on the screen.", false, example = { drawMetrics() }) { drawMetrics() }

    private fun GuiGraphicsExtractor.drawMetrics(): Pair<Int, Int> {
        var pos = 1
        if (showTPS) pos = drawMetric("TPS: ", ServerUtils.averageTps.toFixed(1), "20.0", pos)
        if (showFPS) pos = drawMetric("FPS: ", "${mc.fps}", "9999", pos)
        if (showPing) pos = drawMetric("Ping: ", "${ServerUtils.averagePing}ms", "9999ms", pos)

        return if (direction == Direction.HORIZONTAL) pos to mc.font.lineHeight + 1
        else getStringWidth("Ping: 9999ms ") + 1 to pos
    }

    private fun GuiGraphicsExtractor.drawMetric(label: String, value: String, widest: String, pos: Int): Int {
        val horizontal = direction == Direction.HORIZONTAL
        val x = if (horizontal) pos else 1
        val y = if (horizontal) 1 else pos
        text(label, x, y, nameColor)
        text(value, x + getStringWidth(label), y, valueColor)
        return pos + if (horizontal) getStringWidth("$label$widest ") else mc.font.lineHeight
    }
}
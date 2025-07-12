package me.odinmain.features.impl.render

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.features.Module
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.drawStringWidth

object PerformanceHUD : Module(
    name = "Performance HUD",
    description = "Displays your current ping, FPS and server's TPS."
) {
    private val nameColor by ColorSetting("Name", Color(50, 150, 220), desc = "The color of the stat information.")
    private val valueColor by ColorSetting("Value", Colors.WHITE, desc = "The color of the stat values.")
    private val showFPS by BooleanSetting("Show FPS", true, desc = "Shows the FPS in the HUD.")
    private val showTPS by BooleanSetting("Show TPS", true, desc = "Shows the TPS in the HUD.")
    private val showPing by BooleanSetting("Show Ping", true, desc = "Shows the ping in the HUD.")

    private val hud by HUD("Performance HUD", "Shows performance information on the screen.") {
        if (!showFPS && !showTPS && !showPing) return@HUD 0f to 0f
        var width = 1f

        if (showTPS) {
            width += drawStringWidth("TPS: ", width, 1f, nameColor, true)
            width += drawStringWidth("${ServerUtils.averageTps.toInt()} ", width, 1f, valueColor, true)
        }
        if (showFPS) {
            width += drawStringWidth("FPS: ", width, 1f, nameColor, true)
            width += drawStringWidth("${mc.debug?.split(" ")[0]?.toIntOrNull() ?: 0} ", width, 1f, valueColor, true)
        }
        if (showPing) {
            width += drawStringWidth("Ping: ", width, 1f, nameColor, true)
            width += drawStringWidth("${ServerUtils.averagePing.toInt()}ms", width, 1f, valueColor, true)
        }

        width to mc.fontRendererObj.FONT_HEIGHT
    }
}
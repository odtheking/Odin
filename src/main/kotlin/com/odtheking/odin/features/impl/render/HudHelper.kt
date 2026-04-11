package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.textDim

object HudHelper : Module(
    name = "HUD Helper",
    description = "Shows keybind hints in the HUD editor.",
    toggled = true
) {
    private val hud by HUD(name, description, false) { example ->
        if (!example && mc.screen != HudManager) return@HUD 0 to 0

        val white = Colors.WHITE
        val aqua  = Colors.MINECRAFT_AQUA
        val gray  = Colors.MINECRAFT_GRAY
        val green = Colors.MINECRAFT_GREEN
        val red   = Colors.MINECRAFT_RED

        val col2 = 70
        val lineH = mc.font.lineHeight
        var y = 0
        var maxWidth = 0

        fun line(key: String, desc: String) {
            textDim(key, 0, y, aqua)
            textDim(desc, col2, y, gray)
            maxWidth = maxOf(maxWidth, col2 + getStringWidth(desc))
            y += lineH
        }

        // Grid status
        val gridLabel = "Grid: "
        val gridVal = if (HudManager.gridEnabled) "ON (size: ${HudManager.gridSize})" else "OFF"
        val gridColor = if (HudManager.gridEnabled) green else red
        textDim(gridLabel, 0, y, white)
        textDim(gridVal, getStringWidth(gridLabel), y, gridColor)
        maxWidth = maxOf(maxWidth, getStringWidth(gridLabel) + getStringWidth(gridVal))
        y += lineH + 2

        textDim("Controls:", 0, y, white)
        maxWidth = maxOf(maxWidth, getStringWidth("Controls:"))
        y += lineH + 1

        line("G",       "Toggle grid snap")
        line("+  /  -", "Adjust grid size")
        line("Drag",    "Move element")
        line("Scroll",  "Resize element")
        line("Arrows",  "Move 1px")
        line("H / V",   "Center horizontally / vertically")

        maxWidth to y
    }
}

package me.odinclient.ui.waypoint.elements

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.ui.waypoint.WaypointGUI
import me.odinclient.utils.gui.GuiUtils.scissor
import me.odinclient.utils.gui.MouseHandler
import java.awt.Color

class AreaButton(
    val area: String,
    private val mouseHandler: MouseHandler
) {

    private var displayX = 0f
    private var displayY = 0f
    var width = 0f

    fun draw(vg: VG, x: Float, y: Float, shouldDrawLine: Boolean): Float {
        displayX = x
        displayY = y
        nanoVG(vg.instance) {
            width = getTextWidth(area, 10f, Fonts.REGULAR)
            scissor(0f, 25f, 480f, 25f) {
                if (area == WaypointGUI.displayArea) drawRoundedRect(x - 3, y - 10, width + 7, 18f, 5f, Color(32, 32, 32).rgb)

                drawText(area, x, y, Color.WHITE.rgb, 10, Fonts.REGULAR)

                if (shouldDrawLine) drawLine(x - 5, y - 7, x - 5, y + 7, 0.7, Color.WHITE.rgb)
            }
        }
        return width + 10
    }

    fun mouseClicked(): Boolean = mouseHandler.isAreaHovered(displayX - 3, displayY - 10, width + 7, 18f)
}
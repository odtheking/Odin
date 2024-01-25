package me.odinmain.ui.waypoint.elements

import me.odinmain.features.impl.render.WaypointManager
import me.odinmain.features.impl.render.WaypointManager.Waypoint
import me.odinmain.ui.waypoint.WaypointGUI
import me.odinmain.ui.waypoint.WaypointGUI.mouseHandler
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.Fonts
import me.odinmain.utils.render.gui.animations.impl.ColorAnimation
import me.odinmain.utils.render.gui.drawDynamicTexture
import me.odinmain.utils.render.gui.loadImage
import me.odinmain.utils.render.gui.roundedRectangle
import net.minecraft.client.renderer.texture.DynamicTexture

class WaypointElement(val waypoint: Waypoint) {
    private val name get() = waypoint.name.noControlCodes
    private val colorAnimation = ColorAnimation(150)
    var y = 0f

    private val inputFields = arrayOf(
        WaypointInputField(name, mouseHandler, 12f, Fonts.REGULAR),
        WaypointInputField(waypoint.x, "x:", mouseHandler, 10f, Fonts.REGULAR),
        WaypointInputField(waypoint.y, "y:", mouseHandler, 10f, Fonts.REGULAR),
        WaypointInputField(waypoint.z, "z:", mouseHandler, 10f, Fonts.REGULAR),
    )
    private val trash = DynamicTexture(loadImage("/assets/odinmain/waypoint/trash.png"))

    fun drawScreen(): Int {
        roundedRectangle(15, y, 450, 30, Color(13, 14, 15), 5f)

        val color = colorAnimation.get(waypoint.color, Color(21, 22, 23), waypoint.shouldShow)
        roundedRectangle(20, y + 6, 18, 18, color, 5f)

        val trashColor = if (mouseHandler.isAreaHovered(442f, y + 6, 18f, 18f)) Color(192, 192, 192).rgba else -1
        drawDynamicTexture(trash, 442f, y + 6, 18f, 18f) // get better svg it looks so pixelated

        var currentX = 40f
        for (i in inputFields) {
            i.x = currentX
            currentX += i.draw(currentX, y + 15, -1)
        }
        return 40
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (mouseHandler.isAreaHovered(20f, y + 6, 18f, 18f)) {
                colorAnimation.start()
                waypoint.shouldShow = !waypoint.shouldShow
                return true
            } else if (mouseHandler.isAreaHovered(442f, y + 6, 18f, 18f)) {
                WaypointManager.removeWaypoint(waypoint)
                WaypointGUI.list.remove(this)

                return true
            }

            for ((index, i) in inputFields.withIndex()) {
                if (!i.mouseClicked()) saveCurrent(index) else return true
            }
        }
        return false
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        for ((index, i) in inputFields.withIndex()) {
            if (keyCode != 28) {
                i.keyTyped(typedChar, keyCode)
                continue
            }
            saveCurrent(index)
        }
    }

    private fun saveCurrent(index: Int) {
        val field = inputFields[index]
        field.listening = false
        val value = field.text.toIntOrNull() ?: 0
        if (index != 0) field.text = value.toString()
        when (index) {
            0 -> waypoint.name = "§f${field.text}"
            1 -> waypoint.x = value
            2 -> waypoint.y = value
            3 -> waypoint.z = value
        }
    }
}

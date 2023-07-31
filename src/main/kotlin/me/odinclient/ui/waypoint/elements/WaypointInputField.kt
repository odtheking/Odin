package me.odinclient.ui.waypoint.elements

import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.utils.render.gui.MouseHandler
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.animations.impl.LinearAnimation
import org.lwjgl.input.Keyboard
import java.awt.Color

class WaypointInputField(
    defaultText: Any,
    private val prefix: String? = null,
    private val onlyNumbers: Boolean,
    private val mouseHandler: MouseHandler,
    private val size: Float,
    private val font: Font
) {
    constructor(defaultText: String, mouseHandler: MouseHandler, size: Float, font: Font) : this(defaultText, null, false, mouseHandler, size, font)

    constructor(defaultText: Int, prefix: String, mouseHandler: MouseHandler, size: Float, font: Font) : this(defaultText, prefix, true, mouseHandler, size, font)

    private inline val display get() = (if (prefix != null) "$prefix " else "") + text
    var text: String = defaultText.toString()
    var listening = false

    var x = 0f
    var y = 0f
    private var width = 0f

    private val outlineAnimation = EaseInOut(250)
    private val inputAnimation = LinearAnimation<Float>(325)

    fun draw(vg: VG, x: Float, y: Float, color: Int): Float {
        nanoVG(vg.instance) {
            this@WaypointInputField.y = y
            this@WaypointInputField.width =
                if (width == 0f) getTextWidth(display, size, font) + 12
                else inputAnimation.get(width, getTextWidth(display, size, font) + 12)

            val alpha = outlineAnimation.get(0f, 255f, !listening).toInt()
            drawHollowRoundedRect(x, y - size / 2 - 3.5, width, size + 6, 5f, Color(30, 32, 34, alpha).rgb, 0.75)
            drawText(display, x + 6, y + 0.5, color, size, font)

        }
        return width + 10
    }

    fun mouseClicked(): Boolean {
        if (mouseHandler.isAreaHovered(x, y - size / 2 - 3, width, size + 6)) {
            listening = true
            outlineAnimation.start()
            return true
        }
        listening = false
        return false
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!listening) return
        if (keyCode == 14 && text.isNotEmpty())
            text = text.dropLast(1)
        else if (((text.isEmpty() && keyCode == Keyboard.KEY_MINUS) || isAllowedCharacter(typedChar)) && text.length <= 30)
            text += typedChar

        inputAnimation.start(true)
    }

    private fun isAllowedCharacter(character: Char): Boolean =
        ((character >= ' ' && !onlyNumbers) || (character.isDigit() && onlyNumbers)) && character.code != 127
}
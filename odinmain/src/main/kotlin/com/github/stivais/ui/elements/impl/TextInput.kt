package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.events.Key
import com.github.stivais.ui.events.Mouse
import me.odinmain.features.impl.render.Animations
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.modMessage
import org.lwjgl.input.Keyboard

// Unfinished
class TextInput(var text: String, constraints: Constraints?) : Element(constraints) {

    private var caret: Int = text.length
        set(value) {
            field = value.coerceIn(0, text.length)
        }

    // uses to check if width should be recalculated as it is expensive to do so
    private var previousHeight = 0f

    override fun prePosition() {
        if (!renders) return
        height = constraints.height.get(this, Type.H)
        if (previousHeight != height) {
            previousHeight = height
            val newWidth =  renderer.textWidth(text, height)
            (constraints.width as Pixel).pixels = newWidth
        }
    }

    //var dragg

    private val fontSize = 30f

    private var startSelect = 0
    private var endSelect = 0

    private var cx: Float = 0f

    private var cy: Float = 0f

    override fun draw() {
        renderer.rect(x - 4, y - 4, renderer.textWidth(text, fontSize) + 8f, fontSize + 4, Color.BLACK.rgba)
        if (startSelect != endSelect) {
            val x1 = x + renderer.textWidth(text.substring(0, startSelect.toInt()), size = fontSize)
            val x2 = x + renderer.textWidth(text.substring(0, endSelect.toInt()), size = fontSize)
            renderer.rect(x1, y, x2 - x1, fontSize, Color.BLUE.rgba, 9f)
        }

        renderer.text(text, x, y, 30f, Color.WHITE.rgba)

        renderer.rect(x + cx, y + cy, 1f, 28f, Color.WHITE.rgba)
    }


    init {
        registerEvent(Key.CodePressed(-1, true)) {
            handleKeyPress((this as Key.CodePressed).code, ui)
            true
        }

        registerEvent(Mouse.Clicked(0)) {
            modMessage("focused")

            ui.focus(this@TextInput)
            true
        }

        registerEvent(Mouse.Clicked(null)) {
            modMessage("clicked")
            caret = ((ui.mx - x) / renderer.textWidth("a", 30f)).toInt()
            startSelect = caret
            endSelect = caret
            modMessage("caret: $caret")
            positionCaret()
            true
        }
    }

    private fun handleKeyPress(code: Int, ui: UI) {
        when (code) {
            Keyboard.KEY_RIGHT -> right(1)

            Keyboard.KEY_LEFT -> left(1)

            Keyboard.KEY_RETURN -> insert('\n')

            Keyboard.KEY_BACK -> remove(1)

            Keyboard.KEY_DELETE -> delete(1)

            Keyboard.KEY_ESCAPE -> ui.unfocus()

            //Keyboard.KEY_X -> if (isKeyComboCtrlX(code)) cutText()

            else -> if (Keyboard.getEventCharacter().isLetter()) insert(Keyboard.getEventCharacter())
        }
        if (isKeyComboCtrlA(code)) startSelect = 0; endSelect = text.length

        if (isKeyComboCtrlC(code)) copyToClipboard(text.substring(startSelect.toInt(), endSelect.toInt()))

        if (isKeyComboCtrlV(code)) insert(getClipboardString())

        modMessage("caret: $caret, text: $text, startSelect: $startSelect, endSelect: $endSelect")
        positionCaret()
    }

    fun left(amount: Int) {
        caret -= amount
    }

    fun right(amount: Int) {
        caret += amount
    }

    private fun insert(string: Char) {
        val before = caret
        text = text.substring(0, caret) + string + text.substring(caret)
        if (text.length != before) caret++
    }

    private fun insert(string: String) {
        text = text.substring(0, caret) + string + text.substring(caret)
    }

    private fun remove(amount: Int) {
        if (caret - amount < 0) return
        text = text.substring(0, caret - amount) + text.substring(caret)
        caret -= amount
    }

    private fun delete(amount: Int) {
        if (caret + amount > text.length) return
        text = text.substring(0, caret) + text.substring(caret + amount)
    }

    private fun positionCaret() {
        val currLine = getCurrentLine()
        cx = renderer.textWidth(currLine.first, size = fontSize) + Animations.x
        cy = Animations.y + currLine.second * fontSize
    }

    private fun getCurrentLine(): Pair<String, Int> {
        var i = 0
        var ls = 0
        var line = 0

        for (chr in text) {
            i++
            if (chr == '\n') {
                ls = i
                line++
            }
            if (i == caret) {
                return text.substring(ls, caret).substringBefore('\n') to line
            }
        }
        return "" to 0
    }
}
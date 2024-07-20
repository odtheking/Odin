package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.*
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.events.*
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.devMessage
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import kotlin.math.*

/*
* TODO
* - needs cleanup
*/
class TextInput(
    text: String,
    private val placeholder: String,
    position: Positions? = null,
    size: Size,
    val widthLimit: Size? = null,
    censor: Boolean = false,
    val onlyNumbers: Boolean = false,
    val onTextChange: (string: String) -> Unit = {}
) : Text(text, UI.defaultFont, Color.WHITE, position, size) {

    override var text: String = text
        set(value) {
            if (field == value) return
            field = value
            redraw = true
            previousHeight = 0f

            // text input stuff
            if (history.last() != value) history.add(value)
            if (censorInput) censorCache = buildString { repeat(text.length) { append('*') } }

            updateCaret()
            onTextChange(value)
        }

    private val _text: String
        get() = if (censorInput) censorCache!! else text

    private val placeholderColor: Color = color { color!!.rgba.darker(0.75) }

    var censorInput = censor
        set(value) {
            if (value == field) return
            censorCache = if (value) buildString { repeat(text.length) { append('*') } } else null
            redraw = true
            previousHeight = 0f
            field = value
        }

    private var censorCache: String? = if (censor) buildString { repeat(text.length) { append('*') } } else null

    private var caretPosition: Int = text.length
        set(value) {
            field = value.coerceIn(0, text.length)
            updateCaret()
            // start animation
        }

    private var selectionStart: Int = caretPosition
        set(value) {
            field = value.coerceIn(0, text.length)
            selectionX = renderer.textWidth(text.substring(0, field), size = height)
        }

    private var dragging = false

    private var offs = 0f

    private var caretX: Float = 0f

    private var history: MutableList<String> = mutableListOf(text)
    private var selectionX: Float = 0f
    private var lastClickTime: Long = 0L
    private var clickCount: Int = 0

    override fun preSize() {
        super.preSize()
        if (widthLimit != null) {
            val maxW = widthLimit.get(this, Type.W)
            if (width >= maxW) {
                offs = width - maxW
                width = maxW
            } else {
                offs = 0f
            }
        }
    }

    override fun getTextWidth(): Float {
        return when {
            text.isEmpty() -> renderer.textWidth(placeholder, height)
            censorInput -> renderer.textWidth(censorCache!!, height)
            else -> super.getTextWidth()
        }
    }

    override fun draw() {
        val focused = ui.isFocused(this)
        // cleanup
        if (selectionStart != caretPosition) {
            val startX = x + min(selectionX, caretX).toInt()
            val endX = x + max(selectionX, caretX).toInt()
            renderer.rect(startX - offs, y, endX - startX, height - 4, Color.RGB(0, 0, 255, 0.5f).rgba)
        }
        when {
            text.isEmpty() && !focused -> {
                renderer.text(placeholder, x, y, height, placeholderColor.rgba)
            }
            censorInput -> {
                renderer.text(censorCache!!, x, y, height, color!!.get(this))
            }
            else -> {
                renderer.text(text, x - offs, y, height, Color.WHITE.rgba)
            }
        }

        if (focused) {
            renderer.rect(x + caretX - offs, y, 1f, height - 2, Color.WHITE.rgba)
        }
//        renderer.hollowRect(x, y, width, height, 1f, Color.WHITE.rgba)
    }

    init {
        Focused.Gained register {
            Keyboard.enableRepeatEvents(true)
            setCaretPositionBasedOnMouse()
            selectionStart = caretPosition
            false
        }
        Focused.Lost register {
            selectionStart = caretPosition
            Keyboard.enableRepeatEvents(false)
            false
        }
        // todo bring function inside this
        Key.CodePressed(-1, true) register {
            handleKeyPress(it.code)
            true
        }

        Focused.Clicked(0) register {
            if (it.button == 0) {
                val current = System.currentTimeMillis()
                if (current - lastClickTime < 300) clickCount++ else clickCount = 1
                lastClickTime = current

                when (clickCount) {
                    1 -> {
                        setCaretPositionBasedOnMouse()
                        if (!isShiftKeyDown()) selectionStart = caretPosition
                    }
                    2 -> {
                        val word = getCurrentWord(caretPosition)
                        selectionStart = word?.first ?: (caretPosition - 1)
                        caretPosition = word?.second ?: caretPosition
                    }
                    3 -> {
                        selectionStart = 0
                        caretPosition = text.length
                    }
                }
                dragging = true
                return@register true
            }
            false
        }
        Mouse.Moved register {
            if (dragging) {
                setCaretPositionBasedOnMouse()
                return@register true
            }
            lastClickTime = 0L
            false
        }
        Mouse.Clicked(0) register {
            dragging = true
            ui.focus(this)
            false
        }
        Mouse.Released(0) register {
            dragging = false
            false
        }
    }

    private fun handleKeyPress(code: Int) {
        val eventChar = Keyboard.getEventCharacter()
        when {
            isKeyComboCtrlA(code) -> {
                caretPosition = this.text.length
                selectionStart = 0
            }

            isKeyComboCtrlC(code) -> writeToClipboard(getSelectedText(selectionStart, caretPosition))

            isKeyComboCtrlV(code) -> {
                val clipboard = GuiScreen.getClipboardString()
                caretPosition = (caretPosition + clipboard.length)
                insert(GuiScreen.getClipboardString())
            }

            isKeyComboCtrlX(code) -> {
                writeToClipboard(getSelectedText(selectionStart, caretPosition))
                insert("")
            }

            isKeyComboCtrlZ(code) -> {
                if (history.size > 1) {
                    history.removeAt(history.size - 1)
                    this.text = history.last()
                }
            }

            else -> when (code) {

                Keyboard.KEY_BACK -> {
                    if (isCtrlKeyDown()) deleteWords(-1)
                    else deleteFromCaret(-1)
                }

                Keyboard.KEY_HOME -> {
                    if (isShiftKeyDown()) moveCaretBy(-caretPosition)
                    else moveSelectionAndCaretBy(-caretPosition)
                }

                Keyboard.KEY_LEFT -> {
                    if (isShiftKeyDown()) {
                        if (isCtrlKeyDown()) moveCaretBy(getNthWordFromCaret(-1) - caretPosition)
                        else moveCaretBy(-1)
                    }
                    else {
                        if (isCtrlKeyDown()) moveSelectionAndCaretBy(getNthWordFromCaret(-1) - caretPosition)
                        else moveSelectionAndCaretBy(-1)
                    }
                }

                Keyboard.KEY_RIGHT -> {
                    if (isShiftKeyDown()) {
                        if (isCtrlKeyDown()) moveCaretBy(getNthWordFromCaret(1) - caretPosition)
                        else moveCaretBy(1)
                    }
                    else {
                        if (isCtrlKeyDown()) moveSelectionAndCaretBy(getNthWordFromCaret(1) - caretPosition)
                        else moveSelectionAndCaretBy(1)
                    }
                }

                Keyboard.KEY_END -> {
                    if (isShiftKeyDown()) moveCaretBy(text.length - caretPosition)
                    else moveSelectionAndCaretBy(text.length - caretPosition)
                }

                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> {
                    selectionStart = 0
                    caretPosition = 0
                    ui.unfocus()
                }

                Keyboard.KEY_DELETE -> {
                    if (isCtrlKeyDown()) deleteWords(1)
                    else deleteFromCaret(1)
                }

                else -> {
                    if (onlyNumbers) {
                        val insert = eventChar.isDigit() || (eventChar == '-' && text.isEmpty()) || (eventChar == '.' && !text.contains('.'))
                        if (insert) insert(eventChar.toString())
                    } else {
                        if (ChatAllowedCharacters.isAllowedCharacter(eventChar)) insert(eventChar.toString())
                    }
                }
            }
        }
        devMessage("cursorPosition: $caretPosition, startSelect: $selectionStart string: ${this.text}")
    }

    // cleanup everything under here
    // remove unnecessary stuff and variables

    private fun moveCaretBy(amount: Int) {
        caretPosition = (caretPosition + amount).coerceIn(0, text.length)
    }

    private fun moveSelectionAndCaretBy(amount: Int) {
        caretPosition = (caretPosition + amount).coerceIn(0, text.length)
        selectionStart = caretPosition
    }

    private fun insert(text: String) {
        if (text.length >= 30) return
        val min = min(caretPosition, selectionStart)
        val max = max(caretPosition, selectionStart)
        val maxLength = 30 - text.length + max - min

        val addedText = ChatAllowedCharacters.filterAllowedCharacters(text).take(maxLength)

        this.text = this.text.take(min) + addedText + this.text.substring(max)

        moveSelectionAndCaretBy(min - selectionStart + addedText.length)
        selectionStart = caretPosition
    }

    private fun deleteWords(num: Int) = deleteFromCaret(getNthWordFromCaret(num) - caretPosition)

    private fun deleteFromCaret(num: Int) {
        if (text.isEmpty()) return
        if (selectionStart != caretPosition) insert("")
        else {
            val target = (caretPosition + num).coerceIn(0, text.length)
            if (num < 0) {
                text = text.removeRange(target, caretPosition)
                moveSelectionAndCaretBy(num)
            } else
                text = text.removeRange(caretPosition, target)
        }
    }

    private fun getNthWordFromCaret(n: Int): Int = getNthWordFromPos(n, caretPosition)

    private fun getNthWordFromPos(n: Int, pos: Int): Int {
        var i = pos
        val negative = n < 0

        repeat(abs(n)) {
            if (negative) {
                while (i > 0 && text[i - 1].code == 32) i--
                while (i > 0 && text[i - 1].code != 32) i--
            } else {
                while (i < text.length && text[i].code == 32) i++
                i = text.indexOf(32.toChar(), i)
                if (i == -1) {
                    i = text.length
                }
            }
        }
        return i
    }

    private fun getCurrentWord(pos: Int): Pair<Int, Int>? {
        val length = text.length
        var start = pos
        var end = pos

        // Move start left until a space or the beginning of the string
        while (start > 0 && text[start - 1].code != 32) {
            start--
        }

        // Move end right until a space or the end of the string
        while (end < length && text[end].code != 32) {
            end++
        }

        // Check if the word is surrounded by text or is at the edges of the string
        val isStartValid = start == 0 || text[start - 1].code == 32
        val isEndValid = end == length || text[end].code == 32

        return if (isStartValid && isEndValid && start != end) Pair(start, end) else null
    }

    private fun getSelectedText(selectionStart: Int, caretPosition: Int): String {
        return substringSafe(text, selectionStart, caretPosition)
    }

    private fun updateCaret() {
        val str = if (caretPosition <= text.length) _text.substring(0, caretPosition) else ""
        caretX = renderer.textWidth(str, size = height)
    }

    private fun setCaretPositionBasedOnMouse() {
        caretPosition = if (this.text.isEmpty()) 0
        else ((ui.mx - x) / ((width - offs) / this.text.length)).toInt().coerceIn(0, this.text.length)
    }

    private fun substringSafe(string: String, start: Int, end: Int): String {
        if (start == end) return ""
        val s: Int
        val e: Int
        // check if start is bigger than end, if so, swap them
        if (start > end) { s = end; e = start } else { s = start; e = end }
        return string.substring(s, max(e, string.length - 1))
    }
}
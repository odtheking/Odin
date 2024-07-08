package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.events.*
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.devMessage
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import kotlin.math.abs

/*
* TODO
* - needs cleanup
* - make options, like to stop typing if limit reached or continue
* - only number options
* - censor input like '******'
*/
class TextInput(
    text: String,
    private val placeholder: String,
    constraints: Constraints? = null,
    val widthLimit: Size? = null,
    val lockIfLimit: Boolean = false,
    val onlyNumbers: Boolean = false,
    val onTextChange: (string: String) -> Unit = {}
) : Text(text, UI.defaultFont, Color.WHITE, constraints, constraints?.height ?: 50.percent) {

    private val placeholderColor: Color = Color { color!!.rgba.brighter(1.5) }

    private var string: String = text
        set(value) {
            if (lock) {
                if (value.length > field.length) return
            }
            text = value
            field = value
            textWidth = renderer.textWidth(value, size = height)
            if (history.last() != value) history.add(value)
            positionCaret()
            redraw()
//            ui.needsRedraw = true
            onTextChange(value)
        }

    private var caretPosition: Int = string.length
        set(value) {
            field = value.coerceIn(0, string.length)
            positionCaret()
            // start animation
        }

    private var selectionStart: Int = caretPosition
        set(value) {
            field = value.coerceIn(0, string.length)
            selectionX = renderer.textWidth(string.substring(0, field), size = height)
        }

    private var textWidth = 0f
    private var isHeld = false

    // experimental
    var lock = false
        set(value) {
            if (value != field) {
                redraw = true
//                ui.needsRedraw = true
            }
            field = value
        }

    var offs = 0f

    private var caretX: Float = 0f

    private var history: MutableList<String> = mutableListOf(string)
    private var selectionX: Float = 0f
    private var lastClickTime: Long = 0L
    private var clickCount: Int = 0

    override fun preSize() {
        super.preSize()
        if (widthLimit != null) {
            val maxW = widthLimit.get(this, Type.W)
            if (width >= maxW) {
                if (lockIfLimit) {
                    lock = true
                } else {
                    offs = width - maxW
                }
                width = maxW
            } else {
                lock = false
                offs = 0f
            }
        }
    }

    override fun getTextWidth(): Float {
        if (text.isEmpty()) return renderer.textWidth(placeholder, height)
        return super.getTextWidth()
    }

    override fun draw() {
        // cleanup
        if (selectionStart != caretPosition) {
            val startX = x + min(selectionX, caretX).toInt()
            val endX = x + max(selectionX, caretX).toInt()
            renderer.rect(startX - offs, y, endX - startX, height - 4, Color.RGB(0, 0, 255, 0.5f).rgba)
        }

        if (text.isNotEmpty()) {
            renderer.text(text, x - offs, y, height, Color.WHITE.rgba)
        } else {
            renderer.text(placeholder, x, y, height, placeholderColor.rgba)
        }

        if (ui.isFocused(this)) {
            renderer.rect(x + caretX - offs, y, 1f, height - 2, Color.WHITE.rgba)
        }
    }

    init {
        registerEvent(Focused.Gained) {
            setCaretPositionBasedOnMouse(x, textWidth - offs, ui.mx)
            selectionStart = caretPosition
            false
        }
        registerEvent(Key.CodePressed(-1, true)) {
            handleKeyPress((this as Key.CodePressed).code)
            true
        }

        registerEvent(Focused.Clicked()) {
//            modMessage("a ${(this as Focused.Clicked).button}")
            if ((this as Focused.Clicked).button == 0) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 300) clickCount++ else clickCount = 1
                lastClickTime = currentTime

                when (clickCount) {
                    1 -> {
                        setCaretPositionBasedOnMouse(x, textWidth - offs, ui.mx)
                        if (!isShiftKeyDown()) selectionStart = caretPosition
                    }

                    2 -> {
                        val word = getCurrentWord(caretPosition)
                        selectionStart = word?.first ?: (caretPosition - 1)
                        caretPosition = word?.second ?: caretPosition
                    }

                    3 -> {
                        selectionStart = 0
                        caretPosition = string.length
                    }
                }
                isHeld = true
                return@registerEvent true
            }
            false
        }

        registerEvent(Mouse.Moved) {
            if (isHeld) setCaretPositionBasedOnMouse(x, textWidth - offs, ui.mx)
            lastClickTime = 0L
            true
        }

        registerEvent(Mouse.Released(0)) {
            isHeld = false
            true
        }

        registerEvent(Mouse.Clicked(0)) {
            ui.focus(this@TextInput)
            Keyboard.enableRepeatEvents(true)
            textWidth = renderer.textWidth(string, size = height)
            true
        }
    }

    private fun handleKeyPress(code: Int) {
        val eventChar = Keyboard.getEventCharacter()
        when {
            isKeyComboCtrlA(code) -> {
                caretPosition = string.length
                selectionStart = 0
            }

            isKeyComboCtrlC(code) -> copyToClipboard(getSelectedText(selectionStart, caretPosition))

            isKeyComboCtrlV(code) -> insert(getClipboardString())

            isKeyComboCtrlX(code) -> {
                copyToClipboard(getSelectedText(selectionStart, caretPosition))
                insert("")
            }

            isKeyComboCtrlZ(code) -> {
                if (history.size > 1) {
                    history.removeAt(history.size - 1)
                    string = history.last()
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
                    if (isShiftKeyDown()) moveCaretBy(string.length - caretPosition)
                    else moveSelectionAndCaretBy(string.length - caretPosition)
                }

                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> {
                    selectionStart = 0
                    caretPosition = 0
                    Keyboard.enableRepeatEvents(false)
                    ui.unfocus()
                }

                Keyboard.KEY_DELETE -> {
                    if (isCtrlKeyDown()) deleteWords(1)
                    else deleteFromCaret(1)
                }

                else -> {

                    if (onlyNumbers) {
                        when {
                            string.isEmpty() && eventChar == '-' -> insert(eventChar.toString())
                            !string.contains('-') && eventChar == '.' -> insert(eventChar.toString())
                            eventChar != '.' && eventChar != '-' && eventChar.isDigit()  -> insert(eventChar.toString())
                        }
                    } else
                        if (ChatAllowedCharacters.isAllowedCharacter(eventChar)) insert(eventChar.toString())
                }
            }
        }
        devMessage("cursorPosition: $caretPosition, startSelect: $selectionStart string: $string")
    }

    // cleanup everything under here
    // remove unnecessary stuff and variables

    private fun moveCaretBy(amount: Int){
        caretPosition = (caretPosition + amount).coerceIn(0, string.length)
    }

    private fun moveSelectionAndCaretBy(amount: Int) {
        caretPosition = (caretPosition + amount).coerceIn(0, string.length)
        selectionStart = caretPosition
    }

    private fun insert(text: String) {
        if (text.length >= 30) return
        val min = kotlin.math.min(caretPosition, selectionStart)
        val max = kotlin.math.max(caretPosition, selectionStart)
        val maxLength = 30 - text.length + max - min

        val addedText = ChatAllowedCharacters.filterAllowedCharacters(text).take(maxLength)

        string = string.take(min) + addedText + string.substring(max)

        moveSelectionAndCaretBy(min - selectionStart + addedText.length)
        selectionStart = caretPosition
    }

    private fun deleteWords(num: Int) = deleteFromCaret(getNthWordFromCaret(num) - caretPosition)

    private fun deleteFromCaret(num: Int) {
        if (string.isEmpty()) return
        if (selectionStart != caretPosition) insert("")
        else {
            val target = (caretPosition + num).coerceIn(0, text.length)
            if (num < 0) {
                string = string.removeRange(target, caretPosition)
                moveSelectionAndCaretBy(num)
            } else
                string = string.removeRange(caretPosition, target)
        }
    }

    private fun getNthWordFromCaret(n: Int): Int = getNthWordFromPos(n, caretPosition)

    private fun getNthWordFromPos(n: Int, pos: Int): Int {
        var i = pos
        val negative = n < 0

        repeat(abs(n)) {
            if (negative) {
                while (i > 0 && string[i - 1].code == 32) i--
                while (i > 0 && string[i - 1].code != 32) i--
            } else {
                while (i < string.length && string[i].code == 32) i++
                i = string.indexOf(32.toChar(), i)
                if (i == -1) {
                    i = string.length
                }
            }
        }
        return i
    }

    private fun getCurrentWord(pos: Int): Pair<Int, Int>? {
        val length = string.length
        var start = pos
        var end = pos

        // Move start left until a space or the beginning of the string
        while (start > 0 && string[start - 1].code != 32) {
            start--
        }

        // Move end right until a space or the end of the string
        while (end < length && string[end].code != 32) {
            end++
        }

        // Check if the word is surrounded by text or is at the edges of the string
        val isStartValid = start == 0 || string[start - 1].code == 32
        val isEndValid = end == length || string[end].code == 32

        return if (isStartValid && isEndValid && start != end) Pair(start, end) else null
    }

    private fun getSelectedText(selectionStart: Int, caretPosition: Int): String {
        return string.substring(min(selectionStart, caretPosition).toInt(), max(selectionStart, caretPosition).toInt())
    }

    private fun positionCaret() {
        caretX = renderer.textWidth(string, size = height)
    }

    private fun setCaretPositionBasedOnMouse(x: Float, textWidth: Float, mx: Float) {
        caretPosition = if (string.isEmpty()) 0
        else ((mx - x) / (textWidth / string.length)).toInt().coerceIn(0, string.length)
    }
}
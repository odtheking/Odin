package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.events.Focused
import com.github.stivais.ui.events.Key
import com.github.stivais.ui.events.Mouse
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import kotlin.math.abs

// todo: rework, I undid everything cuz was no good or smth
class TextInput(
    text: String,
    private val placeholder: String,
    constraints: Constraints? = null,
    val onTextChange: (string: String) -> Unit = {}
) : Text(text, UI.defaultFont, Color.WHITE, constraints, 50.percent) {

    private val placeholderColor: Color = Color { color!!.rgba.brighter(0.7) }

    private var string: String = text
        set(value) {
            text = value
            field = value
            textWidth = renderer.textWidth(value, size = height).toInt()
            if (history.last() != value) history.add(value)
            positionCursor()
            ui.needsRedraw = true
            onTextChange(value)
        }

    private var cursorPosition: Int = string.length
        set(value) {
            field = value.coerceIn(0, string.length)
            positionCursor()
            // start animation
        }

    private var selectionStart: Int = cursorPosition
        set(value) {
            field = value.coerceIn(0, string.length)
            selectionX = renderer.textWidth(string.substring(0, field), size = height)
        }

    private var textWidth = 0
    private var isHeld = false

    private var cursorX: Float = 0f
    private var cursorY: Float = 0f

    private var history: MutableList<String> = mutableListOf(string)
    private var selectionX: Float = 0f
    private var lastClickTime: Long = 0L
    private var clickCount: Int = 0

    override fun draw() {
//        renderer.rect(x - 4, y - 4, textWidth + 10f, height + 4, Color.BLACK.rgba, 9f)
        if (selectionStart != cursorPosition) {
            val startX = x + min(selectionX, cursorX).toInt()
            val endX = x + max(selectionX, cursorX).toInt()
            renderer.rect(startX, y, endX - startX, height - 4, Color.RGB(0, 0, 255, 0.5f).rgba)
        }

        renderer.text(text, x, y, height, Color.WHITE.rgba)

        renderer.rect(x + cursorX, y + cursorY, 1f, height - 2, Color.WHITE.rgba) // caret
    }

    init {
        registerEvent(Focused.Gained) {
            modMessage("Focus gain")
            true
        }
        registerEvent(Focused.Lost) {
            modMessage("Focus lost")
            true
        }
        registerEvent(Key.CodePressed(-1, true)) {
            handleKeyPress((this as Key.CodePressed).code)
            true
        }

        registerEvent(Mouse.Clicked(null)) {
            if ((this as Mouse.Clicked).button == 0) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 300) clickCount++ else clickCount = 1
                lastClickTime = currentTime

                when (clickCount) {
                    1 -> {
                        setCursorPositionBasedOnMouse(x, textWidth, ui.mx)
                        if (!isShiftKeyDown()) selectionStart = cursorPosition
                    }

                    2 -> {
                        val word = getCurrentWord(cursorPosition)
                        selectionStart = word?.first ?: (cursorPosition - 1)
                        cursorPosition = word?.second ?: cursorPosition
                    }

                    3 -> {
                        selectionStart = 0
                        cursorPosition = string.length
                    }
                }
                isHeld = true
            }
            false
        }

        registerEvent(Mouse.Moved) {
            if (isHeld) setCursorPositionBasedOnMouse(x, textWidth, ui.mx)
            lastClickTime = 0L
            true
        }

        registerEvent(Mouse.Released(0)) {
            isHeld = false
            true
        }

        registerEvent(Mouse.Clicked(0)) {
            modMessage("focused")
            ui.focus(this@TextInput)
            Keyboard.enableRepeatEvents(true)
            textWidth = renderer.textWidth(string, size = height).toInt()
            positionCursor()
            true
        }
    }


    private fun handleKeyPress(code: Int) {
        when {
            isKeyComboCtrlA(code) -> {
                cursorPosition = string.length
                selectionStart = 0
            }

            isKeyComboCtrlC(code) -> copyToClipboard(getSelectedText(selectionStart, cursorPosition))

            isKeyComboCtrlV(code) -> insert(getClipboardString())

            isKeyComboCtrlX(code) -> {
                copyToClipboard(getSelectedText(selectionStart, cursorPosition))
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
                    else deleteFromCursor(-1)
                }

                Keyboard.KEY_HOME -> {
                    if (isShiftKeyDown()) moveCursorBy(-cursorPosition)
                    else moveSelectionAndCursorBy(-cursorPosition)
                }

                Keyboard.KEY_LEFT -> {
                    if (isShiftKeyDown()) {
                        if (isCtrlKeyDown()) moveCursorBy(getNthWordFromCursor(-1) - cursorPosition)
                        else moveCursorBy(-1)
                    }
                    else {
                        if (isCtrlKeyDown()) moveSelectionAndCursorBy(getNthWordFromCursor(-1) - cursorPosition)
                        else moveSelectionAndCursorBy(-1)
                    }
                }

                Keyboard.KEY_RIGHT -> {
                    if (isShiftKeyDown()) {
                        if (isCtrlKeyDown()) moveCursorBy(getNthWordFromCursor(1) - cursorPosition)
                        else moveCursorBy(1)
                    }
                    else {
                        if (isCtrlKeyDown()) moveSelectionAndCursorBy(getNthWordFromCursor(1) - cursorPosition)
                        else moveSelectionAndCursorBy(1)
                    }
                }

                Keyboard.KEY_END -> {
                    if (isShiftKeyDown()) moveCursorBy(string.length - cursorPosition)
                    else moveSelectionAndCursorBy(string.length - cursorPosition)
                }

                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_RETURN -> {
                    selectionStart = 0
                    cursorPosition = 0
                    Keyboard.enableRepeatEvents(false)
                    ui.unfocus()
                }

               // Keyboard.KEY_RETURN -> string = insert("\n", string, selectionStart, cursorPosition, byPassFilter = true)

                Keyboard.KEY_DELETE -> {
                    if (isCtrlKeyDown()) deleteWords(1)
                    else deleteFromCursor(1)
                }

                else -> {
                    if (ChatAllowedCharacters.isAllowedCharacter(Keyboard.getEventCharacter()))
                        insert(Keyboard.getEventCharacter().toString())
                }
            }
        }
        devMessage("cursorPosition: $cursorPosition, startSelect: $selectionStart string: $string")
    }

    private fun moveCursorBy(amount: Int){
        cursorPosition = (cursorPosition + amount).coerceIn(0, string.length)
    }

    private fun moveSelectionAndCursorBy(amount: Int) {
        cursorPosition = (cursorPosition + amount).coerceIn(0, string.length)
        selectionStart = cursorPosition
    }

    private fun insert(text: String) {
        if (text.length >= 30) return
        val min = kotlin.math.min(cursorPosition, selectionStart)
        val max = kotlin.math.max(cursorPosition, selectionStart)
        val maxLength = 30 - text.length + max - min

        val addedText = ChatAllowedCharacters.filterAllowedCharacters(text).take(maxLength)

        string = string.take(min) + addedText + string.substring(max)

        moveSelectionAndCursorBy(min - selectionStart + addedText.length)
        selectionStart = cursorPosition
    }

    private fun deleteWords(num: Int) = deleteFromCursor(getNthWordFromCursor(num) - cursorPosition)

    private fun deleteFromCursor(num: Int) {
        if (string.isEmpty()) return
        if (selectionStart != cursorPosition) insert("")
        else {
            val target = (cursorPosition + num).coerceIn(0, text.length)
            if (num < 0) {
                string = string.removeRange(target, cursorPosition)
                moveSelectionAndCursorBy(num)
            } else
                string = string.removeRange(cursorPosition, target)
        }
    }

    private fun getNthWordFromCursor(n: Int): Int = getNthWordFromPos(n, cursorPosition)

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

    private fun getSelectedText(selectionStart: Int, cursorPosition: Int): String {
        return string.substring(min(selectionStart, cursorPosition).toInt(), max(selectionStart, cursorPosition).toInt())
    }

    private fun positionCursor() {
        val currLine = getCurrentLine()
        cursorX = renderer.textWidth(currLine.first, size = height)
        cursorY = currLine.second * height
    }

    private fun getCurrentLine(): Pair<String, Int> {
        var i = 0
        var ls = 0
        var line = 0

        for (chr in string) {
            i++
            if (chr == '\n') {
                ls = i
                line++
            }
            if (i == cursorPosition)
                return text.substring(ls, cursorPosition).substringBefore('\n') to line
        }
        return "" to 0
    }

    private fun setCursorPositionBasedOnMouse(x: Float, textWidth: Int, mx: Float) {
        if (string.isEmpty()) return
        cursorPosition = ((mx - x) / (textWidth / string.length)).toInt().coerceIn(0, string.length)
    }
}
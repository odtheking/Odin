package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.events.Key
import com.github.stivais.ui.events.Mouse
import me.odinmain.features.impl.render.Animations
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import kotlin.math.abs


class TextInput(text: String, constraints: Constraints?) : Text(text, Color.WHITE, constraints, 30.px) {

    private var cursorPosition: Int = text.length
        set(value) {
            field = value.coerceIn(0, text.length)
            positionCursor()
        }

    private var selectionStart: Int = cursorPosition
        set(value) {
            field = value.coerceIn(0, text.length)
            selectionX = renderer.textWidth(text.substring(0, value), size = height)
        }

    private var textWidth = 0
    private var isHeld = false

    private var cursorX: Float = 0f
    private var cursorY: Float = 0f
    private var selectionX: Float = 0f

    override fun draw() {
        renderer.rect(x - 4, y - 4, textWidth + 8f, height + 4, Color.BLACK.rgba, 9f)
        if (selectionStart != cursorPosition) {
            val startX = x + min(selectionX, cursorX).toInt()
            val endX = x + max(selectionX, cursorX).toInt()
            renderer.rect(startX, y, endX - startX, height - 2, Color.RGB(0, 0, 255, 0.5f).rgba)
        }

        renderer.text(text, x, y, height, Color.WHITE.rgba)

        renderer.rect(x + cursorX, y + cursorY, 1f, height - 2, Color.WHITE.rgba)
    }


    init {
        registerEvent(Key.CodePressed(-1, true)) {
            handleKeyPress((this as Key.CodePressed).code, ui)
            true
        }

        registerEvent(Mouse.Clicked(0)) {
            modMessage("focused")
            ui.focus(this@TextInput)
            Keyboard.enableRepeatEvents(true)
            textWidth = renderer.textWidth(text, size = height).toInt()
            true
        }

        registerEvent(Mouse.Clicked(null)) {
            cursorPosition = ((ui.mx - x) / renderer.textWidth("a", 30f)).toInt()
            if (!isShiftKeyDown()) selectionStart = cursorPosition
            isHeld = true
            true
        }

        registerEvent(Mouse.Moved) {
            if (isHeld)
                cursorPosition = ((ui.mx - x) / renderer.textWidth("a", 30f)).toInt()
            true
        }

        registerEvent(Mouse.Released(0)) {
            isHeld = false
            true
        }
    }

    private fun handleKeyPress(code: Int, ui: UI) {
        when {
            isKeyComboCtrlA(code) -> {
                cursorPosition = text.length
                selectionStart = 0
            }

            isKeyComboCtrlC(code) -> copyToClipboard(text.substring(selectionStart, cursorPosition))

            isKeyComboCtrlV(code) -> insert(getClipboardString())

            isKeyComboCtrlX(code) -> {
                copyToClipboard(text.substring(selectionStart, cursorPosition))
                text = text.substring(0, selectionStart) + text.substring(cursorPosition)
            }

            else -> when (code) {

                Keyboard.KEY_BACK -> {
                    if (isCtrlKeyDown()) deleteWords(-1)
                    else deleteFromCursor(-1)
                    selectionStart = cursorPosition
                }

                Keyboard.KEY_HOME -> {
                    if (isShiftKeyDown()) selectionStart = 0
                    else cursorPosition = 0
                }

                Keyboard.KEY_LEFT -> {
                    if (isShiftKeyDown())
                        if (isCtrlKeyDown())
                            moveCursorBy(getNthWordFromPos(-1, cursorPosition) - cursorPosition)
                        else
                            moveCursorBy(-1)

                    else if (isCtrlKeyDown()) {
                        cursorPosition = getNthWordFromPos(-1, cursorPosition)
                        selectionStart = cursorPosition
                    } else {
                        moveCursorBy(-1)
                        selectionStart = cursorPosition
                    }
                }

                Keyboard.KEY_RIGHT -> {
                    if (isShiftKeyDown())
                        if (isCtrlKeyDown())
                            moveCursorBy(getNthWordFromPos(1, cursorPosition) - cursorPosition)
                        else
                            moveCursorBy(1)

                    else if (isCtrlKeyDown()) {
                        cursorPosition = getNthWordFromPos(1, cursorPosition)
                        selectionStart = cursorPosition
                    } else {
                        moveCursorBy(1)
                        selectionStart = cursorPosition
                    }
                }

                Keyboard.KEY_END -> {
                    if (isShiftKeyDown()) selectionStart = text.length
                    else cursorPosition = text.length
                }

                Keyboard.KEY_ESCAPE, Keyboard.KEY_NUMPADENTER -> {
                    selectionStart = 0
                    cursorPosition = 0
                    Keyboard.enableRepeatEvents(false)
                    ui.unfocus()
                }

                Keyboard.KEY_RETURN -> insert("\n")

                Keyboard.KEY_DELETE -> {
                    if (isCtrlKeyDown()) deleteWords(1)
                    else deleteFromCursor(1)
                    selectionStart = cursorPosition
                }

                else -> {
                    if (ChatAllowedCharacters.isAllowedCharacter(Keyboard.getEventCharacter())) {
                        insert(Keyboard.getEventCharacter().toString())
                        selectionStart = cursorPosition
                    }
                }
            }
        }

        devMessage("caret: $cursorPosition, text: $text, startSelect: $selectionStart,")
    }

    private fun moveCursorBy(moveAmount: Int) {
        cursorPosition += moveAmount
    }

    private val maxStringLength = 256

    private fun insert(string: String) {
        val min = kotlin.math.min(cursorPosition, selectionStart)
        val max = kotlin.math.max(cursorPosition, selectionStart)
        val maxLength = maxStringLength - text.length + max - min

        val addedText = ChatAllowedCharacters.filterAllowedCharacters(string).take(maxLength)

        text = text.take(min) + addedText + text.substring(max)

        moveCursorBy(min - selectionStart + addedText.length)
        textWidth = renderer.textWidth(text, size = height).toInt()
        selectionStart = cursorPosition
    }

    private fun positionCursor() {
        val currLine = getCurrentLine()
        cursorX = renderer.textWidth(currLine.first, size = height) + Animations.x
        cursorY = Animations.y + currLine.second * height
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
            if (i == cursorPosition) {
                return text.substring(ls, cursorPosition).substringBefore('\n') to line
            }
        }
        return "" to 0
    }

    private fun getNthWordFromCursor(n: Int): Int = getNthWordFromPos(n, cursorPosition)

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

    private fun deleteWords(num: Int) = deleteFromCursor(getNthWordFromCursor(num) - cursorPosition)

    private fun deleteFromCursor(num: Int) {
        if (text.isEmpty()) return
        if (selectionStart != cursorPosition) {
            insert("")
        } else {
            val negative = num < 0
            val target = (cursorPosition + num).coerceIn(0, text.length)
            if (negative) {
                text = text.removeRange(target, cursorPosition)
                moveCursorBy(num)
            } else {
                text = text.removeRange(cursorPosition, target)
            }
        }
    }
}
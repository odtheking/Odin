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
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import kotlin.math.abs


// Unfinished
class TextInput(var text: String, constraints: Constraints?) : Element(constraints) {

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

    private var cursorPosition: Int = text.length
        set(value) {
            field = value.coerceIn(0, text.length)
        }

    private var selectionStart: Int = 0
        set(value) {
            field = value.coerceIn(0, text.length)
        }

    private val selectedText: String
        get() = if (selectionStart < cursorPosition) text.substring(selectionStart, cursorPosition) else text.substring(cursorPosition, selectionStart)


    private val fontSize = 30f
    private var isHeld = false

    private var cx: Float = 0f

    private var cy: Float = 0f

    override fun draw() {
        renderer.rect(x - 4, y - 4, renderer.textWidth(text, fontSize) + 8f, fontSize + 4, Color.BLACK.rgba, 9f)
        if (selectionStart != cursorPosition) {
            val min = kotlin.math.min(cursorPosition, selectionStart)
            val max = kotlin.math.max(cursorPosition, selectionStart)
            val x1 = x + renderer.textWidth(text.substring(0, min), size = fontSize)
            val x2 = x + renderer.textWidth(text.substring(0, max), size = fontSize)
            renderer.rect(x1, y, x2 - x1, fontSize - 2, Color.RGB(0, 0, 255, 0.5f).rgba)
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
            Keyboard.enableRepeatEvents(true)
            true
        }

        registerEvent(Mouse.Clicked(null)) {
            if (isShiftKeyDown())
                cursorPosition = ((ui.mx - x) / renderer.textWidth("a", 30f)).toInt()
            else {
                cursorPosition = ((ui.mx - x) / renderer.textWidth("a", 30f)).toInt()
                selectionStart = cursorPosition
            }
            isHeld = true
            positionCursor()
            true
        }

        registerEvent(Mouse.Moved) {
            if (isHeld)
                cursorPosition = ((ui.mx - x) / renderer.textWidth("a", 30f)).toInt()
                positionCursor()
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
                        if (isCtrlKeyDown()) {
                            val prev = getNthWordFromPos(-1, cursorPosition)
                            modMessage("prev: $prev")
                            moveCursorBy(prev - cursorPosition)
                            modMessage("move by ${prev - cursorPosition}")
                        } else
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
                        if (isCtrlKeyDown()) {
                            val next = getNthWordFromPos(1, cursorPosition)
                            modMessage("next: $next")
                            moveCursorBy(next - cursorPosition)
                            modMessage("move by ${next - cursorPosition}")
                        } else
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
        positionCursor()
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
        selectionStart = cursorPosition
    }

    private fun positionCursor() {
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
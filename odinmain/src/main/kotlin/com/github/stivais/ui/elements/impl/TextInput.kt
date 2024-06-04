package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.events.Key
import com.github.stivais.ui.events.Mouse
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard


class TextInput(text: String, constraints: Constraints?) : Text(text, Color.WHITE, constraints, 30.px) {

    private var string: String = text
        set(value) {
            text = value
            field = value
            textWidth = renderer.textWidth(value, size = height).toInt()
            cursorPosition = value.length
        }

    private var cursorPosition: Int = text.length
        set(value) {
            field = value.coerceIn(0, text.length)
            positionCursor(text, cursorPosition, height)
        }

    private var selectionStart: Int = cursorPosition
        set(value) {
            field = value.coerceIn(0, text.length)
            selectionX = renderer.textWidth(text.substring(0, value), size = height)
        }

    private val lineNumber: Int
        get() = text.count { it == '\n' } + 1



    private var textWidth = 0
    private var isHeld = false

    private var selectionX: Float = 0f

    override fun draw() {
        renderer.rect(x - 4, y - 4, textWidth + 8f, 4 + height * lineNumber, Color.BLACK.rgba, 9f)
        if (selectionStart != cursorPosition) {
            val startX = x + min(selectionX, cursorX).toInt()
            val endX = x + max(selectionX, cursorX).toInt()
            renderer.rect(startX, y, endX - startX, height - 4, Color.RGB(0, 0, 255, 0.5f).rgba)
        }

        val width = constraints.width.get(this, Type.W)
        modMessage("width: $width")

        //wrappedTextBounds()

        text.split("\n").forEachIndexed { index, text ->
            renderer.text(text, x, y + height * index, height, Color.WHITE.rgba)
        }

        renderer.rect(x + cursorX, y + cursorY, 1f, height - 2, Color.WHITE.rgba)
    }

    fun wrappedText() {
        val width = constraints.width.get(this, Type.W)
        val height = constraints.height.get(this, Type.H)
        val text = text.split("\n")
        var y = y
        text.forEachIndexed { index, text ->
            //renderer.wrappedText(text, x, y, width, height, Color.WHITE.rgba)
            y += height
        }
    }

    private var lastClickTime = 0L

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
            if (System.currentTimeMillis() - lastClickTime < 300) { // Double click
                selectionStart = getNthWordFromCursor(string, -1, cursorPosition)
                cursorPosition = getNthWordFromCursor(string, 1, cursorPosition)
            } else {
                setCursorPositionBasedOnMouse()
                if (!isShiftKeyDown()) selectionStart = cursorPosition
            }
            lastClickTime = System.currentTimeMillis()
            isHeld = true
            true
        }

        registerEvent(Mouse.Moved) {
            if (isHeld) setCursorPositionBasedOnMouse()
            lastClickTime = 0L
            true
        }

        registerEvent(Mouse.Released(0)) {
            isHeld = false
            true
        }
    }

    private fun setCursorPositionBasedOnMouse() {
        if (text.isEmpty()) return
        cursorPosition = ((ui.mx - x) / (textWidth / text.length)).toInt().coerceIn(0, text.length)
    }

    private fun handleKeyPress(code: Int, ui: UI) {
        when {
            isKeyComboCtrlA(code) -> {
                cursorPosition = text.length
                selectionStart = 0
            }

            isKeyComboCtrlC(code) -> copyToClipboard(getSelectedText(string, selectionStart, cursorPosition))

            isKeyComboCtrlV(code) -> {
                string = insert(getClipboardString(), string, selectionStart, cursorPosition)
                selectionStart = cursorPosition
            }

            isKeyComboCtrlX(code) -> {
                copyToClipboard(getSelectedText(string, selectionStart, cursorPosition))
                string = insert("", string, selectionStart, cursorPosition)
            }

            else -> when (code) {

                Keyboard.KEY_BACK -> {
                    string = if (isCtrlKeyDown()) deleteWords(string, selectionStart, cursorPosition, -1)
                    else deleteFromCursor(string, selectionStart, cursorPosition, -1)
                    selectionStart = cursorPosition
                }

                Keyboard.KEY_HOME -> {
                    if (isShiftKeyDown()) selectionStart = 0
                    else cursorPosition = 0
                }

                Keyboard.KEY_LEFT -> {
                    if (isShiftKeyDown())
                        cursorPosition = if (isCtrlKeyDown()) moveCursorBy(cursorPosition, getNthWordFromPos(string, -1, cursorPosition) - cursorPosition)
                            else moveCursorBy(cursorPosition, -1)

                    else {
                        cursorPosition = if (isCtrlKeyDown()) getNthWordFromCursor(text, -1, cursorPosition)
                            else moveCursorBy(cursorPosition, -1)
                        selectionStart = cursorPosition
                    }
                }

                Keyboard.KEY_RIGHT -> {
                    if (isShiftKeyDown())
                        cursorPosition = if (isCtrlKeyDown()) moveCursorBy(cursorPosition, getNthWordFromPos(string, 1, cursorPosition) - cursorPosition)
                            else moveCursorBy(cursorPosition, 1)

                    else {
                        cursorPosition = if (isCtrlKeyDown()) getNthWordFromPos(string, 1, cursorPosition)
                            else moveCursorBy(cursorPosition, 1)
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

                Keyboard.KEY_RETURN -> string = insert("\n", string, selectionStart, cursorPosition, byPassFilter = true)

                Keyboard.KEY_DELETE -> {
                    string = if (isCtrlKeyDown()) deleteWords(string, selectionStart, cursorPosition, 1)
                    else deleteFromCursor(string, selectionStart, cursorPosition, 1)
                    selectionStart = cursorPosition
                }

                else -> {
                    if (ChatAllowedCharacters.isAllowedCharacter(Keyboard.getEventCharacter())) {
                        string = insert(Keyboard.getEventCharacter().toString(), string, selectionStart, cursorPosition)
                        selectionStart = cursorPosition
                    }
                }
            }
        }
        devMessage("caret: $cursorPosition, text: $text, startSelect: $selectionStart,")
    }
}
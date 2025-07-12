package me.odinmain.utils.ui

import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.rendering.NVGRenderer
import me.odinmain.utils.writeToClipboard
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import kotlin.math.max
import kotlin.math.min

class TextInputHandler(
    private val textProvider: () -> String,
    private val textSetter: (String) -> Unit
) {
    private inline val text: String get() = textProvider()

    var x = 0f
    var y = 0f
    var width = 0f
    var height = 18f

    private var caret = text.length
        set(value) {
            if (field == value) return
            field = value.coerceIn(0, text.length)
            caretBlinkTime = System.currentTimeMillis()
        }

    private var selection = text.length
    private var selectionWidth = 0f
    private var textOffset = 0f
    private var caretX = 0f

    private var caretBlinkTime = System.currentTimeMillis()
    private var lastClickTime = 0L
    private var listening = false
    private var dragging = false
    private var clickCount = 1

    private val history = mutableListOf<String>()
    private var historyIndex = -1
    private var lastSavedText = ""

    init {
        saveState()
    }

    private var previousMousePos = 0f to 0f

    fun draw(mouseX: Float, mouseY: Float) {
        if (previousMousePos != mouseX to mouseY) mouseDragged(mouseX)
        previousMousePos = mouseX to mouseY

        NVGRenderer.pushScissor(x, y, width, height)
        if (selectionWidth != 0f) NVGRenderer.rect(x + caretX + 4f, y, selectionWidth, height, Colors.MINECRAFT_BLUE.rgba, 4f)
        NVGRenderer.popScissor()

        if (listening) {
            val time = System.currentTimeMillis()
            if (time - caretBlinkTime < 500)
                NVGRenderer.line(x + caretX + 4f - textOffset, y, x + caretX + 4f - textOffset, y + height, 2f, Colors.WHITE.rgba)
            else if (time - caretBlinkTime > 1000)
                caretBlinkTime = System.currentTimeMillis()
        }
        NVGRenderer.pushScissor(x, y, width, height)

        NVGRenderer.text(text, x + 4f - textOffset, y + 2f, height - 2, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        NVGRenderer.popScissor()
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean {
        if (!isAreaHovered(x, y, width, height)) {
            resetState()
            return false
        }
        if (mouseButton != 0) return false

        listening = true
        dragging = true

        val current = System.currentTimeMillis()
        if (current - lastClickTime < 200) clickCount++ else clickCount = 1
        lastClickTime = current

        when (clickCount) {
            1 -> {
                caretFromMouse(mouseX)
                clearSelection()
            }
            2 -> selectWord()
            3 -> selectAll()
            4 -> clickCount = 0
        }
        return true
    }

    fun mouseReleased() {
        dragging = false
    }

    private fun mouseDragged(mouseX: Float) {
        if (dragging) caretFromMouse(mouseX)
    }

    fun keyPressed(keyCode: Int): Boolean {
        if (!listening) return false
        val returnValue = when (keyCode) {
            Keyboard.KEY_BACK -> {

                if (selection != caret) deleteSelection()
                else if (GuiScreen.isCtrlKeyDown()) {
                    val previousSpace = getPreviousSpace()
                    textSetter(text.removeRangeSafe(previousSpace, caret))
                    caret -= if (caret > previousSpace) caret - previousSpace else 0
                } else if (caret != 0) {
                    textSetter(text.dropAt(caret, -1))
                    caret--
                }
                clearSelection()
                selection != caret || GuiScreen.isCtrlKeyDown() || caret != 0
            }

            Keyboard.KEY_DELETE -> {
                if (selection != caret) deleteSelection()
                else if (GuiScreen.isCtrlKeyDown()) {
                    val nextSpace = getNextSpace()
                    textSetter(text.removeRangeSafe(caret, nextSpace))
                    caret = if (caret < nextSpace) caret else nextSpace
                }  else if (caret != text.length) {
                    textSetter(text.dropAt(caret, 1))
                    caret = if (caret < text.length) caret else text.length
                }
                clearSelection()
                selection != caret || GuiScreen.isCtrlKeyDown() || caret != text.length
            }

            Keyboard.KEY_RIGHT -> {
                if (caret != text.length) {
                    caret = if (GuiScreen.isCtrlKeyDown()) getNextSpace() else caret + 1
                    if (!GuiScreen.isShiftKeyDown()) selection = caret
                    true
                } else false
            }

            Keyboard.KEY_LEFT -> {
                if (caret != 0) {
                    caret = if (GuiScreen.isCtrlKeyDown()) getPreviousSpace() else caret - 1
                    if (!GuiScreen.isShiftKeyDown()) selection = caret
                    true
                } else false
            }

            Keyboard.KEY_HOME -> {
                caret = 0
                if (!GuiScreen.isShiftKeyDown()) selection = caret
                true
            }

            Keyboard.KEY_END -> {
                caret = text.length
                if (!GuiScreen.isShiftKeyDown()) selection = caret
                true
            }

            Keyboard.KEY_ESCAPE, Keyboard.KEY_RETURN -> {
                listening = false
                true
            }
            else -> {
                if (GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown()) {
                    when (keyCode) {
                        Keyboard.KEY_V -> {
                            GuiScreen.getClipboardString()?.let { insert(it) }
                            true
                        }

                        Keyboard.KEY_C -> {
                            if (caret != selection) {
                                writeToClipboard(text.substringSafe(caret, selection))
                                true
                            } else false
                        }

                        Keyboard.KEY_X -> {
                            if (caret != selection) {
                                writeToClipboard(text.substringSafe(caret, selection))
                                deleteSelection()
                                true
                            } else false
                        }

                        Keyboard.KEY_A -> {
                            selection = 0
                            caret = text.length
                            true
                        }

                        Keyboard.KEY_W -> {
                            selectWord()
                            true
                        }

                        Keyboard.KEY_Z -> {
                            undo()
                            true
                        }

                        Keyboard.KEY_Y -> {
                            redo()
                            true
                        }

                        else -> false
                    }
                } else false
            }
        }
        updateCaretPosition()
        return returnValue
    }

    fun keyTyped(typedChar: Char): Boolean {
        if (!listening) return false

        insert(ChatAllowedCharacters.filterAllowedCharacters(typedChar.toString()))
        return true
    }

    private fun insert(string: String) {
        if (caret != selection) {
            textSetter(text.removeRangeSafe(caret, selection))
            caret = if (selection > caret) caret else selection
        }
        val tl = text.length
        textSetter(text.substringSafe(0, caret) + string + text.substring(caret))
        if (text.length != tl) caret += string.length
        clearSelection()
        updateCaretPosition()
        saveState()
    }

    private fun deleteSelection() {
        if (caret == selection) return
        textSetter(text.removeRangeSafe(caret, selection))
        caret = if (selection > caret) caret else selection
        saveState()
    }

    private fun caretFromMouse(mouseX: Float) {
        val mx = mouseX - (x + textOffset)

        var currWidth = 0f
        var newCaret = 0

        for (index in text.indices) {
            val charWidth = textWidth(text[index].toString())
            if ((currWidth + charWidth / 2) > mx) break
            currWidth += charWidth
            newCaret = index + 1
        }
        caret = newCaret
        updateCaretPosition()
    }

    private fun updateCaretPosition() {
        if (selection != caret) {
            selectionWidth = textWidth(text.substringSafe(selection, caret))
            if (selection <= caret) selectionWidth *= -1
        } else selectionWidth = 0f

        if (caret != 0) {
            val previousX = caretX
            caretX = textWidth(text.substringSafe(0, caret))

            if (previousX < caretX) {
                if (caretX - textOffset >= width) textOffset = caretX - width
            } else {
                if (caretX - textOffset <= 0f) textOffset = textWidth(text.substringSafe(0, caret - 1))
            }

            if (textOffset > 0 && textWidth(text) - textOffset < width)
                textOffset = (textWidth(text) - width).coerceAtLeast(0f)
        } else {
            caretX = 0f
            textOffset = 0f
        }
    }

    private fun clearSelection() {
        selection = caret
        selectionWidth = 0f
    }

    private fun selectWord() {
        var start = caret
        var end = caret
        while (start > 0 && !text[start - 1].isWhitespace()) start--
        while (end < text.length && !text[end].isWhitespace()) end++

        selection = start
        caret = end
        updateCaretPosition()
    }

    private fun getPreviousSpace(): Int {
        var start = caret
        while (start > 0) {
            if (start != caret && text[start - 1].isWhitespace()) break
            start--
        }
        return start
    }

    private fun getNextSpace(): Int {
        var end = caret
        while (end < text.length) {
            if (end != caret && text[end].isWhitespace()) break
            end++
        }
        return end
    }

    private fun textWidth(text: String): Float = NVGRenderer.textWidth(text, height - 2, NVGRenderer.defaultFont)

    private fun resetState() {
        listening = false
        textOffset = 0f
        clearSelection()
    }

    private fun selectAll() {
        selection = 0
        caret = text.length
        updateCaretPosition()
    }

    private fun saveState() {
        if (text == lastSavedText) return

        if (historyIndex < history.size - 1) history.subList(historyIndex + 1, history.size).clear()

        history.add(text)
        historyIndex = history.size - 1
        lastSavedText = text
    }

    private fun undo() {
        if (historyIndex <= 0) return

        historyIndex--
        textSetter(history[historyIndex])
        caret = text.length
        selection = caret
        lastSavedText = text
    }

    private fun redo() {
        if (historyIndex >= history.size - 1) return

        historyIndex++
        textSetter(history[historyIndex])
        caret = text.length
        selection = caret
        lastSavedText = text
    }

    private fun String.substringSafe(from: Int, to: Int): String {
        val f = min(from, to).coerceAtLeast(0)
        val t = max(to, from)
        if (t > length) return substring(f)
        return substring(f, t)
    }

    private fun String.removeRangeSafe(from: Int, to: Int): String =
        removeRange(min(from, to), max(to, from))

    private fun String.dropAt(at: Int, amount: Int): String =
        removeRangeSafe(at, at + amount)
}
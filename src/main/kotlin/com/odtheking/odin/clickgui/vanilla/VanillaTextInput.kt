package com.odtheking.odin.clickgui.vanilla

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.util.StringUtil
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

class VanillaTextInput(
    private val maxLength: Int,
    private val hardLimit: Int,
    private val getter: () -> String,
    private val setter: (String) -> Unit
) {
    var x      = 0
    var y      = 0
    var width  = 0
    var height = 18
    var placeholder = ""

    private inline val text: String get() = getter()

    private var caret = 0
        set(value) {
            field = value.coerceIn(0, text.length)
            caretBlinkTime = System.currentTimeMillis()
        }

    private var selection     = 0
    private var textOffset    = 0
    private var caretX        = 0

    private var caretBlinkTime = System.currentTimeMillis()
    private var lastClickTime  = 0L
    private var isActive       = false
    private var isDragging     = false
    private var clickCount     = 0

    private val history      = mutableListOf<String>()
    private var historyIndex = -1
    private var lastSaved    = ""

    init {
        caret = text.length
        selection = caret
        lastSaved = text
        historyIndex = 0
        history.add(text)
    }

    fun draw(graphics: GuiGraphics) {
        if (isDragging) updateCaretFromMouse(lastMouseX)

        if (!isActive && text.isEmpty() && placeholder.isNotEmpty()) {
            graphics.drawString(mc.font, placeholder, x + 2, y + 2, Colors.MINECRAFT_GRAY.rgba, false)
            return
        }

        val textX = (x + 4f - textOffset).toInt()

        if (hasSelection()) {
            val selStartPx = textWidth(text.substring(0, selectionStart()))
            val selEndPx   = textWidth(text.substring(0, selectionEnd()))
            val hlX1 = x + 4 - textOffset + selStartPx
            val hlX2 = x + 4 - textOffset + selEndPx
            graphics.enableScissor(x, y, x + width, y + height)
            graphics.fill(hlX1, y, hlX2, y + height, Colors.MINECRAFT_BLUE.rgba)
            graphics.disableScissor()
        }

        graphics.enableScissor(x, y, x + width, y + height)
        graphics.drawString(mc.font, text, textX, y + (height - mc.font.lineHeight) / 2, Colors.WHITE.rgba, false)
        graphics.disableScissor()

        if (isActive && (System.currentTimeMillis() - caretBlinkTime) % 1000 < 500) {
            val cx = (x + 4f - textOffset + caretX).toInt()
            graphics.fill(cx, y + 1, cx + 1, y + height - 1, Colors.WHITE.rgba)
        }
    }

    private var lastMouseX = 0f

    fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        lastMouseX = mouseX.toFloat()
        val hovered = mouseX in x..x + width && mouseY in y..y + height
        if (!hovered) {
            deactivate()
            return false
        }
        if (click.button() != 0) return false

        isActive   = true
        isDragging = true

        val now = System.currentTimeMillis()
        clickCount = if (now - lastClickTime < 400) clickCount + 1 else 1
        lastClickTime = now

        when (clickCount) {
            1    -> {
                updateCaretFromMouse(mouseX.toFloat())
                clearSelection()
            }
            2    -> selectWord()
            else -> {
                selectAll()
                clickCount = 0
            }
        }
        return true
    }

    fun mouseReleased() {
        isDragging = false
    }

    fun keyPressed(input: KeyEvent): Boolean {
        if (!isActive) return false
        val handled = when (input.key) {
            GLFW.GLFW_KEY_BACKSPACE -> handleBackspace(input)
            GLFW.GLFW_KEY_DELETE    -> handleDelete(input)
            GLFW.GLFW_KEY_LEFT      -> moveCaret(-1, input)
            GLFW.GLFW_KEY_RIGHT     -> moveCaret(+1, input)
            GLFW.GLFW_KEY_HOME      -> {
                caret = 0
                if (!input.hasShiftDown()) clearSelection()
                true
            }
            GLFW.GLFW_KEY_END       -> {
                caret = text.length
                if (!input.hasShiftDown()) clearSelection()
                true
            }
            GLFW.GLFW_KEY_ESCAPE,
            GLFW.GLFW_KEY_ENTER     -> {
                isActive = false
                true
            }
            else                    -> handleCtrlShortcut(input)
        }
        if (handled) updateCaretPosition()
        return handled
    }

    fun keyTyped(input: CharacterEvent): Boolean {
        if (!isActive) return false
        val incoming = StringUtil.filterText(input.codepointAsString())
        if (incoming.isEmpty()) return false
        if (text.length >= hardLimit || text.length >= maxLength) return true
        insert(incoming)
        return true
    }

    private fun handleBackspace(input: KeyEvent): Boolean = when {
        hasSelection()         -> {
            deleteSelection()
            true
        }
        input.hasControlDown() -> {
            val start = getPreviousWordBoundary()
            if (start == caret) return false
            setter(text.removeRange(start, caret))
            caret = start
            clearSelection()
            saveState()
            true
        }
        caret > 0 -> {
            setter(text.removeRange(caret - 1, caret))
            caret--
            clearSelection()
            saveState()
            true
        }
        else -> false
    }

    private fun handleDelete(input: KeyEvent): Boolean = when {
        hasSelection()         -> {
            deleteSelection()
            true
        }
        input.hasControlDown() -> {
            val end = getNextWordBoundary()
            if (end == caret) return false
            setter(text.removeRange(caret, end))
            clearSelection()
            saveState()
            true
        }
        caret < text.length -> {
            setter(text.removeRange(caret, caret + 1))
            clearSelection()
            saveState()
            true
        }
        else -> false
    }

    private fun moveCaret(direction: Int, input: KeyEvent): Boolean {
        val shift = input.hasShiftDown()
        val ctrl  = input.hasControlDown()

        if (!shift && hasSelection()) {
            caret = if (direction > 0) selectionEnd() else selectionStart()
            clearSelection()
            return true
        }

        val newCaret = when {
            direction > 0 -> if (ctrl) getNextWordBoundary()    else (caret + 1).coerceAtMost(text.length)
            else          -> if (ctrl) getPreviousWordBoundary() else (caret - 1).coerceAtLeast(0)
        }
        if (newCaret == caret) return false

        caret = newCaret
        if (!shift) clearSelection()
        return true
    }

    private fun handleCtrlShortcut(input: KeyEvent): Boolean {
        if (!input.hasControlDown() || input.hasShiftDown()) return false
        return when (input.key) {
            GLFW.GLFW_KEY_V -> {
                insert(mc.keyboardHandler.clipboard)
                true
            }
            GLFW.GLFW_KEY_C -> if (hasSelection()) {
                mc.keyboardHandler.clipboard = selectedText()
                true
            } else false
            GLFW.GLFW_KEY_X -> if (hasSelection()) {
                mc.keyboardHandler.clipboard = selectedText()
                deleteSelection()
                true
            } else false
            GLFW.GLFW_KEY_A -> {
                selectAll()
                true
            }
            GLFW.GLFW_KEY_Z -> {
                if (historyIndex <= 0) return false
                historyIndex--
                setter(history[historyIndex])
                caret = text.length
                clearSelection()
                lastSaved = text
                updateCaretPosition()
                true
            }
            GLFW.GLFW_KEY_Y -> {
                if (historyIndex >= history.size - 1) return false
                historyIndex++
                setter(history[historyIndex])
                caret = text.length
                clearSelection()
                lastSaved = text
                updateCaretPosition()
                true
            }
            else            -> false
        }
    }

    private fun insert(string: String) {
        if (hasSelection()) {
            setter(text.removeRange(selectionStart(), selectionEnd()))
            caret = selectionStart()
        }
        val pos = caret
        val before = text.length
        val newText = (text.substring(0, pos) + string + text.substring(pos))
            .take(hardLimit).take(maxLength)
        setter(newText)
        caret = pos + (text.length - before).coerceAtLeast(0)
        clearSelection()
        updateCaretPosition()
        saveState()
    }

    private fun deleteSelection() {
        if (!hasSelection()) return
        setter(text.removeRange(selectionStart(), selectionEnd()))
        caret = selectionStart()
        clearSelection()
        saveState()
    }

    private fun clearSelection()        { selection = caret }
    private fun hasSelection(): Boolean  = selection != caret
    private fun selectionStart(): Int    = min(caret, selection)
    private fun selectionEnd(): Int      = max(caret, selection)
    private fun selectedText(): String   = text.substring(selectionStart(), selectionEnd())

    private fun selectWord() {
        var start = caret
        var end = caret
        while (start > 0           && !text[start - 1].isWhitespace()) start--
        while (end   < text.length && !text[end].isWhitespace())       end++
        selection = start
        caret = end
        updateCaretPosition()
    }

    private fun selectAll() {
        selection = 0
        caret = text.length
        updateCaretPosition()
    }

    private fun updateCaretFromMouse(mouseX: Float) {
        val mx = mouseX - (x + 4f - textOffset)
        var offset = 0f
        var newCaret = 0
        for (i in text.indices) {
            val cw = textWidth(text[i].toString())
            if (offset + cw / 2f > mx) break
            offset += cw
            newCaret = i + 1
        }
        if (isDragging && newCaret != caret) selection = if (selection == caret) selection else selection
        caret = newCaret
        updateCaretPosition()
    }

    private fun updateCaretPosition() {
        caretX = textWidth(text.substring(0, caret))
        val visible = width - 8
        when {
            caretX - textOffset > visible -> textOffset = caretX - visible
            caretX - textOffset < 0      -> textOffset = caretX
        }
        val totalW = textWidth(text)
        if (textOffset > 0 && totalW - textOffset < visible)
            textOffset = (totalW - visible).coerceAtLeast(0)
    }

    private fun getPreviousWordBoundary(): Int {
        var i = caret
        while (i > 0 && text[i - 1].isWhitespace())  i--
        while (i > 0 && !text[i - 1].isWhitespace()) i--
        return i
    }

    private fun getNextWordBoundary(): Int {
        var i = caret
        while (i < text.length && !text[i].isWhitespace()) i++
        while (i < text.length &&  text[i].isWhitespace()) i++
        return i
    }

    private fun saveState() {
        if (text == lastSaved) return
        if (historyIndex < history.size - 1) history.subList(historyIndex + 1, history.size).clear()
        history.add(text)
        historyIndex = history.size - 1
        lastSaved = text
    }

    private fun deactivate() {
        isActive = false
        textOffset = 0
        clearSelection()
    }

    private fun textWidth(str: String): Int = mc.font.width(str)
}
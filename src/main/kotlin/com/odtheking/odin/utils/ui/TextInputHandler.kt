package com.odtheking.odin.utils.ui

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.util.StringUtil
import org.lwjgl.glfw.GLFW
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

    private var caret = 0
        set(value) {
            field = value.coerceIn(0, text.length)
            caretBlinkTime = System.currentTimeMillis()
        }

    private var selection = 0
    private var textOffset = 0f
    private var caretX = 0f

    private var caretBlinkTime = System.currentTimeMillis()
    private var lastClickTime = 0L
    private var isActive = false
    private var isDragging = false
    private var clickCount = 0

    private val history = mutableListOf<String>()
    private var historyIndex = -1
    private var lastSavedText = ""

    init {
        caret = text.length
        selection = caret
        lastSavedText = text
        historyIndex = 0
        history.add(text)
    }

    fun draw(mouseX: Float, mouseY: Float) {
        if (isDragging) updateCaretFromMouse(mouseX)

        if (hasSelection()) {
            val selStartX = textWidth(text.substring(0, selectionStart()))
            val selEndX   = textWidth(text.substring(0, selectionEnd()))
            val rectX = x + 4f - textOffset + selStartX
            val rectW = selEndX - selStartX
            NVGRenderer.pushScissor(x, y, width, height)
            NVGRenderer.rect(rectX, y, rectW, height, Colors.MINECRAFT_BLUE.rgba, 4f)
            NVGRenderer.popScissor()
        }

        if (isActive && (System.currentTimeMillis() - caretBlinkTime) % 1000 < 500) {
            val cx = x + 4f - textOffset + caretX
            NVGRenderer.line(cx, y, cx, y + height, 2f, Colors.WHITE.rgba)
        }

        NVGRenderer.pushScissor(x, y, width, height)
        NVGRenderer.text(text, x + 4f - textOffset, y + 2f, height - 2, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        NVGRenderer.popScissor()
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean {
        if (!isAreaHovered(x, y, width, height, true)) {
            deactivate()
            return false
        }
        if (click.button() != 0) return false

        isActive = true
        isDragging = true

        val now = System.currentTimeMillis()
        clickCount = if (now - lastClickTime < 400) clickCount + 1 else 1
        lastClickTime = now

        when (clickCount) {
            1 -> { updateCaretFromMouse(mouseX); clearSelection() }
            2 -> selectWord()
            3 -> { selectAll(); clickCount = 0 }
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
            GLFW.GLFW_KEY_ENTER     -> { isActive = false; true }
            else                    -> handleCtrlShortcut(input)
        }

        if (handled) updateCaretPosition()
        return handled
    }

    fun keyTyped(input: CharacterEvent): Boolean {
        if (!isActive) return false
        insert(StringUtil.filterText(input.codepointAsString()))
        return true
    }

    private fun handleBackspace(input: KeyEvent): Boolean = when {
        hasSelection()         -> { deleteSelection(); true }
        input.hasControlDown() -> {
            val start = getPreviousWordBoundary()
            if (start == caret) return false
            textSetter(text.removeRange(start, caret))
            caret = start
            clearSelection()
            saveState()
            true
        }
        caret > 0 -> {
            textSetter(text.removeRange(caret - 1, caret))
            caret--
            clearSelection()
            saveState()
            true
        }
        else -> false
    }

    private fun handleDelete(input: KeyEvent): Boolean = when {
        hasSelection()         -> { deleteSelection(); true }
        input.hasControlDown() -> {
            val end = getNextWordBoundary()
            if (end == caret) return false
            textSetter(text.removeRange(caret, end))
            clearSelection()
            saveState()
            true
        }
        caret < text.length -> {
            textSetter(text.removeRange(caret, caret + 1))
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
            GLFW.GLFW_KEY_V -> { insert(mc.keyboardHandler.clipboard); true }
            GLFW.GLFW_KEY_C -> if (hasSelection()) { mc.keyboardHandler.clipboard = selectedText(); true } else false
            GLFW.GLFW_KEY_X -> if (hasSelection()) { mc.keyboardHandler.clipboard = selectedText(); deleteSelection(); true } else false
            GLFW.GLFW_KEY_A -> { selectAll(); true }
            GLFW.GLFW_KEY_Z -> { undo(); true }
            GLFW.GLFW_KEY_Y -> { redo(); true }
            else            -> false
        }
    }

    private fun insert(string: String) {
        if (hasSelection()) {
            textSetter(text.removeRange(selectionStart(), selectionEnd()))
            caret = selectionStart()
        }

        val pos = caret
        val lengthBefore = text.length
        textSetter(text.substring(0, pos) + string + text.substring(pos))
        caret = pos + (text.length - lengthBefore).coerceAtLeast(0)

        clearSelection()
        updateCaretPosition()
        saveState()
    }

    private fun deleteSelection() {
        if (!hasSelection()) return
        textSetter(text.removeRange(selectionStart(), selectionEnd()))
        caret = selectionStart()
        clearSelection()
        saveState()
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
        caret = newCaret
        updateCaretPosition()
    }

    private fun updateCaretPosition() {
        caretX = textWidth(text.substring(0, caret))

        val visibleWidth = width - 8f
        when {
            caretX - textOffset > visibleWidth -> textOffset = caretX - visibleWidth
            caretX - textOffset < 0f           -> textOffset = caretX
        }

        val totalWidth = textWidth(text)
        if (textOffset > 0f && totalWidth - textOffset < visibleWidth)
            textOffset = (totalWidth - visibleWidth).coerceAtLeast(0f)
    }

    private fun clearSelection()        { selection = caret }
    private fun hasSelection(): Boolean  = selection != caret
    private fun selectionStart(): Int    = min(caret, selection)
    private fun selectionEnd(): Int      = max(caret, selection)
    private fun selectedText(): String   = text.substring(selectionStart(), selectionEnd())

    private fun selectWord() {
        var start = caret
        var end   = caret
        while (start > 0           && !text[start - 1].isWhitespace()) start--
        while (end   < text.length && !text[end].isWhitespace())       end++
        selection = start
        caret     = end
        updateCaretPosition()
    }

    private fun selectAll() {
        selection = 0
        caret     = text.length
        updateCaretPosition()
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

    private fun deactivate() {
        isActive   = false
        textOffset = 0f
        clearSelection()
    }

    private fun saveState() {
        if (text == lastSavedText) return
        if (historyIndex < history.size - 1) history.subList(historyIndex + 1, history.size).clear()
        history.add(text)
        historyIndex  = history.size - 1
        lastSavedText = text
    }

    private fun undo() {
        if (historyIndex <= 0) return
        historyIndex--
        textSetter(history[historyIndex])
        caret         = text.length
        clearSelection()
        lastSavedText = text
        updateCaretPosition()
    }

    private fun redo() {
        if (historyIndex >= history.size - 1) return
        historyIndex++
        textSetter(history[historyIndex])
        caret         = text.length
        clearSelection()
        lastSavedText = text
        updateCaretPosition()
    }

    private fun textWidth(str: String): Float =
        NVGRenderer.textWidth(str, height - 2, NVGRenderer.defaultFont)
}
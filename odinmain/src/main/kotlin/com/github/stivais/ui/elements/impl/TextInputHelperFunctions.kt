package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.renderer.impl.NVGRenderer
import me.odinmain.utils.max
import me.odinmain.utils.min
import net.minecraft.util.ChatAllowedCharacters
import kotlin.math.abs


var cursorX: Float = 0f
    var cursorY: Float = 0f

    fun moveCursorBy(currentPosition: Int, moveAmount: Int): Int {
        return currentPosition + moveAmount
    }

    fun insert(string: String, currentString: String, selectionStart: Int, cursorPosition: Int, maxStringLength: Int = 256, byPassFilter: Boolean = false): String {
        val min = min(cursorPosition, selectionStart).toInt()
        val max = max(cursorPosition, selectionStart).toInt()
        val maxLength = maxStringLength - currentString.length + max - min

        val addedText = if (!byPassFilter) ChatAllowedCharacters.filterAllowedCharacters(string).take(maxLength) else string.take(maxLength)

        return currentString.take(min) + addedText + currentString.substring(max)
    }

    fun positionCursor(text: String, cursorPosition: Int, fontSize: Float) {
        val currLine = getCurrentLine(text, cursorPosition)
        cursorX = NVGRenderer.textWidth(currLine.first, size = fontSize)
        cursorY = currLine.second * fontSize
    }

    fun getCurrentLine(text: String, cursorPosition: Int): Pair<String, Int> {
        var i = 0
        var ls = 0
        var line = 0

        for (chr in text) {
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

    fun getNthWordFromCursor(text: String, n: Int, cursorPosition: Int): Int = getNthWordFromPos(text, n, cursorPosition)

    fun getNthWordFromPos(text: String, n: Int, pos: Int): Int {
        var i = pos
        val negative = n < 0

        repeat(abs(n)) {
            if (negative) {
                while (i > 0 && text[i - 1].code == 32) i--
                while (i > 0 && text[i - 1].code != 32) i--
            } else {
                while (i < text.length && text[i].code == 32) i++
                i = text.indexOf(32.toChar(), i)
                if (i == -1) i = text.length
            }
        }
        return i
    }

    fun deleteWords(text: String, selectionStart: Int, cursorPosition: Int, num: Int): String {
       return deleteFromCursor(text, selectionStart, cursorPosition, getNthWordFromCursor(text, num, cursorPosition) - cursorPosition)
    }

    fun deleteFromCursor(text: String, selectionStart: Int, cursorPosition: Int, num: Int): String {
        if (text.isEmpty()) return text
        if (selectionStart != cursorPosition) insert("", text, cursorPosition, selectionStart)
        else {
            val target = (cursorPosition + num).coerceIn(0, text.length)
            return if (num < 0) text.removeRange(target, cursorPosition)
            else text.removeRange(cursorPosition, target)
        }
        return text
    }

    fun getSelectedText(text: String, selectionStart: Int, cursorPosition: Int): String {
        return text.substring(min(selectionStart, cursorPosition).toInt(),  max(selectionStart, cursorPosition).toInt())
    }


fun setCursorPositionBasedOnMouse(text: String, x: Float, textWidth: Int, mx: Float): Int {
    if (text.isEmpty()) return 0
    return ((mx - x) / (textWidth / text.length)).toInt().coerceIn(0, text.length)
}
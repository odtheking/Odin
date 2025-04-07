package me.odinmain.font

import gg.essential.elementa.font.FontRenderer
import gg.essential.elementa.font.data.Font
import gg.essential.universal.UMatrixStack
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.TextAlign
import me.odinmain.utils.render.TextPos
import kotlin.math.max

object OdinFont {

    private lateinit var fontRenderer: FontRenderer

    const val REGULAR = 1
    const val BOLD = 2

    fun init() {
        fontRenderer = FontRenderer(Font.fromResource("/assets/odinmain/fonts/Regular"), Font.fromResource("/assets/odinmain/fonts/SemiBold"))
    }

    fun text(text: String, x: Float, y: Float, color: Color, scale: Float, align: TextAlign = TextAlign.Left, verticalAlign: TextPos = TextPos.Middle, shadow: Boolean = false, type: Int = REGULAR) {
        if (color.isTransparent) return
        val drawX = when (align) {
            TextAlign.Left   -> x
            TextAlign.Right  -> x - getTextWidth(text, scale)
            TextAlign.Middle -> x - getTextWidth(text, scale) / 2f
        }

        val drawY = when (verticalAlign) {
            TextPos.Top    -> y
            TextPos.Middle -> y - getTextHeight(text, scale) / 2f
            TextPos.Bottom -> y - getTextHeight(text, scale)
        }

        val typeText = if (type == BOLD) "§l$text" else text

        fontRenderer.drawString(UMatrixStack.Compat.get(), typeText, color.javaColor, drawX, drawY, 1f, scale, shadow)
    }

    fun getTextWidth(text: String, size: Float): Float {
        return fontRenderer.getStringWidth(text, size)
    }

    fun getTextHeight(text: String, size: Float): Float {
        return fontRenderer.getStringHeight(text, size)
    }

    fun wrappedText(text: String, x: Float, y: Float, w: Float, color: Color, size: Float, type: Int = REGULAR, shadow: Boolean = false) {
        if (color.isTransparent) return

        val words = text.split(" ")
        var line = ""
        var currentHeight = y + 2

        for (word in words) {
            if (getTextWidth(line + word, size) > w) {
                text(line, x, currentHeight, color, size, type = type, shadow = shadow)
                line = "$word "
                currentHeight += getTextHeight(line, size + 7)
            }
            else line += "$word "

        }
        text(line, x, currentHeight , color, size, type = type, shadow = shadow)
    }

    fun wrappedTextBounds(text: String, width: Float, size: Float): Pair<Float, Float> {
        val words = text.split(" ")
        var line = ""
        var lines = 1
        var maxWidth = 0f

        for (word in words) {
            if (getTextWidth(line + word, size) > width) {
                maxWidth = max(maxWidth, getTextWidth(line, size))
                line = "$word "
                lines++
            }
            else line += "$word "

        }
        maxWidth = max(maxWidth, getTextWidth(line, size))

        return Pair(maxWidth, lines * getTextHeight(line, size + 3))
    }
}
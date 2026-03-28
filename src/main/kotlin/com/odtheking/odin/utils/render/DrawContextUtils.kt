package com.odtheking.odin.utils.render

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.FormattedCharSequence
import org.joml.Matrix3x2f
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max

fun GuiGraphics.text(text: String, x: Int, y: Int, color: Color = Colors.WHITE, shadow: Boolean = true) {
    drawString(mc.font, text, x, y, color.rgba, shadow)
}

fun GuiGraphics.textDim(text: String, x: Int, y: Int, color: Color = Colors.WHITE, shadow: Boolean = true): Pair<Int, Int> {
    text(text, x, y, color, shadow)
    return mc.font.width(text) to mc.font.lineHeight
}

fun GuiGraphics.text(text: FormattedCharSequence, x: Int, y: Int, color: Color = Colors.WHITE, shadow: Boolean = true) {
    drawString(mc.font, text, x, y, color.rgba, shadow)
}

fun getStringWidth(text: String): Int = mc.font.width(text)

fun GuiGraphics.hollowFill(x: Int, y: Int, width: Int, height: Int, thickness: Int, color: Color) {
    fill(x, y, x + width, y + thickness, color.rgba)
    fill(x, y + height - thickness, x + width, y + height, color.rgba)
    fill(x, y + thickness, x + thickness, y + height - thickness, color.rgba)
    fill(x + width - thickness, y + thickness, x + width, y + height - thickness, color.rgba)
}

fun GuiGraphics.drawLine(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    color: Color,
    lineWidth: Float = 1f
) {
    val dx = x2 - x1
    val dy = y2 - y1

    val half = max(1, (lineWidth / 2f).toInt())

    pose().pushMatrix()
    pose().translate(x1, y1)
    pose().mul(Matrix3x2f().identity().rotate(atan2(dy, dx)))
    fill(0, -half, ceil(hypot(dx, dy)).toInt(), half, color.rgba)
    pose().popMatrix()
}

fun GuiGraphics.roundedFill(x0: Int, y0: Int, x1: Int, y1: Int, color: Int, radius: Int) {
    DrawContextRenderer.roundedFill(this, x0, y0, x1, y1, color, radius.toFloat())
}

fun GuiGraphics.roundedFill(
    x0: Int, y0: Int, x1: Int, y1: Int,
    color: Int, radius: Int, outlineColor: Int, outlineWidth: Float) {
    DrawContextRenderer.roundedFill(
        this, x0, y0, x1, y1, color,
        radius.toFloat(), outlineColor, outlineWidth
    )
}

fun GuiGraphics.roundedOutline(
    x0: Int, y0: Int, x1: Int, y1: Int, outlineColor: Int,
    outlineWidth: Float, radius: Int = 0
) {
    DrawContextRenderer.roundedOutline(
        this, x0, y0, x1, y1, outlineColor,
        outlineWidth, radius.toFloat()
    )
}
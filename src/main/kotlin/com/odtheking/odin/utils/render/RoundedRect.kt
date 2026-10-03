package com.odtheking.odin.utils.render

import com.odtheking.odin.utils.Color.Companion.alpha
import com.odtheking.odin.utils.Color.Companion.blue
import com.odtheking.odin.utils.Color.Companion.green
import com.odtheking.odin.utils.Color.Companion.red
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.resources.Identifier

data class Corners(
    val topLeft: Float,
    val topRight: Float,
    val bottomRight: Float,
    val bottomLeft: Float
) {
    constructor(radius: Float) : this(radius, radius, radius, radius)

    companion object {
        val NONE = Corners(0f)

        fun top(radius: Float) = Corners(radius, radius, 0f, 0f)
        fun bottom(radius: Float) = Corners(0f, 0f, radius, radius)
    }
}

enum class GradientDirection {
    LEFT_TO_RIGHT,
    TOP_TO_BOTTOM,
    TOP_LEFT_TO_BOTTOM_RIGHT,
    BOTTOM_LEFT_TO_TOP_RIGHT,
}

fun GuiGraphicsExtractor.roundedRect(
    x0: Int, y0: Int, x1: Int, y1: Int, color: Int, corners: Corners = Corners.NONE
) = RoundedRectRenderer.submit(this, x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), color, color, color, color, corners, 0f)

fun GuiGraphicsExtractor.roundedRect(
    x0: Int, y0: Int, x1: Int, y1: Int, color: Int, radius: Float
) = roundedRect(x0, y0, x1, y1, color, Corners(radius))

fun GuiGraphicsExtractor.roundedRectClipped(
    x0: Int, y0: Int, x1: Int, y1: Int,
    clipX0: Int, clipY0: Int, clipX1: Int, clipY1: Int,
    color: Int, corners: Corners = Corners.NONE
) = RoundedRectRenderer.submit(
    this, x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), color, color, color, color, corners, 0f,
    clip = ScreenRectangle(clipX0, clipY0, clipX1 - clipX0, clipY1 - clipY0)
)

fun GuiGraphicsExtractor.roundedShadow(
    x0: Int, y0: Int, x1: Int, y1: Int,
    color: Int, blur: Float, corners: Corners = Corners.NONE
) {
    if (blur <= 0f) return
    RoundedRectRenderer.submitShadow(this, x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), color, corners, blur)
}

fun GuiGraphicsExtractor.roundedOutline(
    x0: Int, y0: Int, x1: Int, y1: Int, color: Int, width: Float, corners: Corners = Corners.NONE
) {
    if (width <= 0f) return
    RoundedRectRenderer.submit(this, x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), color, color, color, color, corners, width)
}

fun GuiGraphicsExtractor.roundedOutline(x0: Int, y0: Int, x1: Int, y1: Int, color: Int, width: Float, radius: Float)
= roundedOutline(x0, y0, x1, y1, color, width, Corners(radius))

fun GuiGraphicsExtractor.roundedRectOutlined(
    x0: Int, y0: Int, x1: Int, y1: Int,
    fill: Int, outline: Int, width: Float = 1f, corners: Corners = Corners.NONE
) {
    if (width <= 0f) return roundedRect(x0, y0, x1, y1, fill, corners)

    RoundedRectRenderer.submit(this, x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), fill, fill, fill, fill, corners, -width)
    roundedOutline(x0, y0, x1, y1, outline, width, corners)
}

fun GuiGraphicsExtractor.roundedRectOutlined(x0: Int, y0: Int, x1: Int, y1: Int, fill: Int, outline: Int, width: Float, radius: Float)
= roundedRectOutlined(x0, y0, x1, y1, fill, outline, width, Corners(radius))

fun GuiGraphicsExtractor.roundedGradient(
    x0: Int, y0: Int, x1: Int, y1: Int,
    start: Int, end: Int, direction: GradientDirection = GradientDirection.LEFT_TO_RIGHT,
    corners: Corners = Corners.NONE
) {
    val middle = midpoint(start, end)
    val (topLeft, topRight, bottomRight, bottomLeft) = when (direction) {
        GradientDirection.LEFT_TO_RIGHT -> intArrayOf(start, end, end, start)
        GradientDirection.TOP_TO_BOTTOM -> intArrayOf(start, start, end, end)
        GradientDirection.TOP_LEFT_TO_BOTTOM_RIGHT -> intArrayOf(start, middle, end, middle)
        GradientDirection.BOTTOM_LEFT_TO_TOP_RIGHT -> intArrayOf(middle, end, middle, start)
    }
    RoundedRectRenderer.submit(this, x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), topLeft, topRight, bottomRight, bottomLeft, corners, 0f)
}

fun GuiGraphicsExtractor.circle(centerX: Int, centerY: Int, radius: Float, color: Int) =
    RoundedRectRenderer.submit(
        this, centerX - radius, centerY - radius, centerX + radius, centerY + radius,
        color, color, color, color, Corners(radius), 0f
    )

fun GuiGraphicsExtractor.roundedTexture(
    x0: Int, y0: Int, x1: Int, y1: Int, texture: Identifier,
    tint: Int = Colors.WHITE.rgba, corners: Corners = Corners.NONE
) = RoundedRectRenderer.submit(this, x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), tint, tint, tint, tint, corners, 0f, texture)

private operator fun IntArray.component4(): Int = this[3]

private fun midpoint(a: Int, b: Int): Int =
    (mix(a.alpha, b.alpha) shl 24) or (mix(a.red, b.red) shl 16) or (mix(a.green, b.green) shl 8) or mix(a.blue, b.blue)

private fun mix(a: Int, b: Int): Int = (a + b) / 2
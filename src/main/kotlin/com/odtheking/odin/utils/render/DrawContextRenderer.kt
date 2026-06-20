package com.odtheking.odin.utils.render

import com.odtheking.odin.utils.Color.Companion.alpha
import com.odtheking.odin.utils.Color.Companion.blue
import com.odtheking.odin.utils.Color.Companion.green
import com.odtheking.odin.utils.Color.Companion.red
import net.minecraft.client.gui.GuiGraphicsExtractor

object DrawContextRenderer {

    data class CornerRadii(
        val topLeft: Float,
        val topRight: Float,
        val bottomRight: Float,
        val bottomLeft: Float
    ) {
        companion object {
            val ZERO = CornerRadii(0f, 0f, 0f, 0f)

            fun uniform(radius: Float): CornerRadii {
                val clamped = radius.coerceAtLeast(0f)
                return CornerRadii(clamped, clamped, clamped, clamped)
            }
        }
    }

    data class OutlineStyle(
        val color: Int,
        val width: Float
    )

    enum class GradientDirection {
        LEFT_TO_RIGHT,
        TOP_TO_BOTTOM,
        TOP_LEFT_TO_BOTTOM_RIGHT,
        BOTTOM_LEFT_TO_TOP_RIGHT,
    }

    data class RoundedOptions(
        val radii: CornerRadii = CornerRadii.ZERO,
        val outline: OutlineStyle? = null
    )

    fun roundedFill(context: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int, color: Int, options: RoundedOptions = RoundedOptions())
        = submitRoundedRect(context, x0, y0, x1, y1, color, color, color, color, options)

    fun roundedFill(
        context: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int,
        color: Int, radius: Float, outlineColor: Int = 0, outlineWidth: Float = 0.0f
    ) = roundedFill(context, x0, y0, x1, y1, color, RoundedOptions(CornerRadii.uniform(radius), outlineOrNull(outlineColor, outlineWidth)))

    fun roundedFill(
        context: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int,
        color: Int, radii: CornerRadii, outlineColor: Int = 0, outlineWidth: Float = 0.0f
    ) = roundedFill(context, x0, y0, x1, y1, color, RoundedOptions(radii, outlineOrNull(outlineColor, outlineWidth)))

    fun roundedOutline(
        context: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int,
        outlineColor: Int, outlineWidth: Float, radii: CornerRadii = CornerRadii.ZERO
    ) {
        val transparent = outlineColor and 0x00FFFFFF
        submitRoundedRect(
            context, x0, y0, x1, y1,
            transparent, transparent, transparent, transparent,
            RoundedOptions(radii, outlineOrNull(outlineColor, outlineWidth))
        )
    }

    fun roundedOutline(
        context: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int,
        outlineColor: Int, outlineWidth: Float, radius: Float
    ) = roundedOutline(context, x0, y0, x1, y1, outlineColor, outlineWidth, CornerRadii.uniform(radius))

    fun roundedFillGradient(
        context: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int,
        startColor: Int, endColor: Int, direction: GradientDirection = GradientDirection.LEFT_TO_RIGHT,
        options: RoundedOptions = RoundedOptions()
    ) {
        val (topLeft, topRight, bottomRight, bottomLeft) = gradientCorners(startColor, endColor, direction)
        submitRoundedRect(context, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft, options)
    }

    fun roundedFillGradient(
        context: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int,
        startColor: Int, endColor: Int, direction: GradientDirection,
        radius: Float, outlineColor: Int = 0, outlineWidth: Float = 0f
    ) = roundedFillGradient(context, x0, y0, x1, y1, startColor, endColor, direction,
            RoundedOptions(CornerRadii.uniform(radius), outlineOrNull(outlineColor, outlineWidth)))

    private fun outlineOrNull(color: Int, width: Float): OutlineStyle? {
        val clampedWidth = width.coerceAtLeast(0f)
        return if (clampedWidth > 0.0f) OutlineStyle(color, clampedWidth) else null
    }

    private fun submitRoundedRect(
        guiGraphics: GuiGraphicsExtractor,
        x0: Int, y0: Int, x1: Int, y1: Int,
        topLeftColor: Int, topRightColor: Int, bottomRightColor: Int,
        bottomLeftColor: Int, options: RoundedOptions
    ) {
        RoundRectPIPRenderer.submit(
            guiGraphics, x0, y0, x1, y1,
            topLeftColor, topRightColor, bottomRightColor, bottomLeftColor,
            options.radii.topLeft, options.radii.topRight, options.radii.bottomRight, options.radii.bottomLeft,
            options.outline?.color ?: 0, options.outline?.width ?: 0.0f
        )
    }

    private fun gradientCorners(startColor: Int, endColor: Int, direction: GradientDirection): IntArray {
        val mid = midpointColor(startColor, endColor)

        return when (direction) {
            GradientDirection.LEFT_TO_RIGHT -> intArrayOf(startColor, endColor, endColor, startColor)
            GradientDirection.TOP_TO_BOTTOM -> intArrayOf(startColor, startColor, endColor, endColor)
            GradientDirection.TOP_LEFT_TO_BOTTOM_RIGHT -> intArrayOf(startColor, mid, endColor, mid)
            GradientDirection.BOTTOM_LEFT_TO_TOP_RIGHT -> intArrayOf(mid, endColor, mid, startColor)
        }
    }

    private fun midpointColor(a: Int, b: Int): Int {
        val alpha = midpoint(a.alpha, b.alpha)
        val red = midpoint(a.red, b.red)
        val green = midpoint(a.green, b.green)
        val blue = midpoint(a.blue, b.blue)

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    private fun midpoint(a: Int, b: Int): Int = ((a + b) / 2).coerceIn(0, 255)
}
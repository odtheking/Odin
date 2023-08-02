package me.odinclient.utils.render.gui.nvg

import cc.polyfrost.oneconfig.renderer.NanoVGHelper
import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.renderer.scissor.Scissor
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.dsl.nanoVGHelper
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.nvg.TextAlign.*

/**
 * Makes it more understanding that [nanoVGHelper] is used for rendering and acts like a wrapper.
 *
 * And helps me cope that one config won`t be removed :sob:
 */
inline val renderer: NanoVGHelper
    get() = nanoVGHelper

/**
 * This classes acts like wrapper for nvg context.
 *
 * You can do:
 * ```
 *  NVG {
 *      rect(/*args*/)
 *  }
 *  // instead of:
 *
 *  VG.nanoVG {
 *      drawRect(/*args*/)
 *  }
 */
@JvmInline
value class NVG(val context: Long) {
    operator fun invoke(block: NVG.() -> Unit) = block.invoke(this)
}

/**
 * Creates a new NVG context
 */
fun drawNVG(block: NVG.() -> Unit) = nanoVGHelper.setupAndDraw(false) {
    block(NVG(it))
}

fun NVG.rect(
    x: Float, y: Float, w: Float, h: Float, color: Color, topL: Float, topR: Float, botL: Float, botR: Float
) {
    if (color.isTransparent) return
    renderer.drawRoundedRectVaried(context, x, y, w, h, color.rgba, topL, topR, botL, botR)
}

fun NVG.rect(
    x: Float, y: Float, w: Float, h: Float, color: Color, radius: Float = 0f
) = rect(x, y, w, h, color, radius, radius, radius, radius)

fun NVG.rectOutline(x: Float, y: Float, w: Float, h: Float, color: Color, radius: Float = 0f, thickness: Float) {
    if (color.isTransparent) return
    renderer.drawHollowRoundRect(
        context, x - 1f, y - 1f, w + .5f, h + .5f, color.rgba, radius, thickness
    )
}

fun NVG.gradientRect(x: Float, y: Float, w: Float, h: Float, color1: Color, color2: Color, radius: Float) {
    if (color1.isTransparent && color2.isTransparent) return
    renderer.drawGradientRoundedRect(context, x, y, w, h, color1.rgba, color2.rgba, radius)
}

fun NVG.drawHSBBox(x: Float, y: Float, w: Float, h: Float, color: Color) {
    nanoVGHelper.drawHSBBox(context, x, y, w, h, color.rgba)
    rectOutline(x, y, w, h, Color(38, 38, 38), 8f, 1f)
}

fun NVG.circle(x: Float, y: Float, radius: Float, color: Color) {
    if (color.isTransparent) return
    renderer.drawCircle(context, x, y, radius, color.rgba)
}

fun NVG.text(text: String, x: Float, y: Float, color: Color, size: Float, font: Font, align: TextAlign = Left) {
    if (color.isTransparent) return
    val drawX = when (align) {
        Left -> x
        Right -> x - getTextWidth(text, size, font)
        Middle -> x - getTextWidth(text, size, font) / 2f
    }
    renderer.drawText(context, text, drawX, y, color.rgba, size, font)
}

fun NVG.getTextWidth(text: String, size: Float, font: Font) =
    renderer.getTextWidth(context, text, size, font)

fun NVG.dropShadow(x: Float, y: Float, w: Float, h: Float, blur: Float, spread: Float, radius: Float) =
    renderer.drawDropShadow(context, x, y, w, h, blur, spread, radius)

fun NVG.translate(x: Float, y: Float) =
    renderer.translate(context, x, y)

fun NVG.scale(x: Float, y: Float) =
    renderer.scale(context, x, y)

fun NVG.setAlpha(alpha: Float) =
    renderer.setAlpha(context, alpha)

fun NVG.scissor(x: Float, y: Float, w: Float, h: Float): Scissor {
    return ScissorHelper.INSTANCE.scissor(context, x, y, w, h)
}

fun NVG.resetScissor(scissor: Scissor) {
    ScissorHelper.INSTANCE.resetScissor(context, scissor)
}

fun NVG.image(filePath: String, x: Float, y: Float, width: Float, height: Float, radius: Float, clazz: Class<*>) =
    renderer.drawRoundImage(context, filePath, x, y, width, height, radius, clazz)

enum class TextAlign {
    Left, Middle, Right
}

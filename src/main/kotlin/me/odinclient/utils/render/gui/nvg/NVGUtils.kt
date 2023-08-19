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
 *  ```
 *  @author Stivais
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
    renderer.drawRoundedRectVaried(context, x, y, w, h, color.rgba, topL, topR, botR, botL)
}

fun NVG.rect(
    x: Float, y: Float, w: Float, h: Float, color: Color, radius: Float = 0f
) = rect(x, y, w, h, color, radius, radius, radius, radius)

fun NVG.rectOutline(x: Float, y: Float, w: Float, h: Float, color: Color, radius: Float = 0f, thickness: Float) {
    if (color.isTransparent) return
    renderer.drawHollowRoundRect(
        context, x - .95f, y - .95f, w + .5f, h + .5f, color.rgba, radius - .5f, thickness
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

fun NVG.textWithControlCodes(text: String, x: Float, y: Float, size: Float, font: Font): Float {
    var i = 0
    var color = Color.WHITE
    var xPos = x
    while (i < text.length) {
        val char = text[i]
        if (char == '\u00a7' && i + 1 < text.length) {
            val colorCode = "0123456789abcdefr".indexOf(text.lowercase()[i + 1])
            color = colorCodes[colorCode]

            i += 2
            continue
        }
        text(char.toString(), xPos, y, color, size, font)
        xPos += getTextWidth(char.toString(), size, font)
        i++
    }
    return xPos
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

// Temporary
fun NVG.text(text: String, x: Float, y: Float, color: Color, size: Float, font: Font, align: TextAlign, verticalAlign: TextPos) {
    val drawY = when (verticalAlign) {
        TextPos.Top -> y + size / 2f
        TextPos.Middle -> y
        TextPos.Bottom -> y - size / 2f
    }
    text(text, x, drawY, color, size, font, align)
}

fun NVG.getTextWidth(text: String, size: Float, font: Font) =
    renderer.getTextWidth(context, text, size, font)

fun NVG.dropShadow(x: Float, y: Float, w: Float, h: Float, blur: Float, spread: Float, radius: Float) =
    renderer.drawDropShadow(context, x, y, w, h, blur, spread, radius)

fun NVG.translate(x: Float, y: Float) =
    renderer.translate(context, x, y)

fun NVG.resetTransform() =
    renderer.resetTransform(context)

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

fun NVG.image(filePath: String, x: Float, y: Float, w: Float, h: Float, radius: Float, clazz: Class<*>) =
    renderer.drawRoundImage(context, filePath, x, y, w, h, radius, clazz)

fun NVG.wrappedText(text: String, x: Float, y: Float, w: Float, h: Float, color: Color, size: Float, font: Font) {
    if (color.isTransparent) return
    renderer.drawWrappedString(context, text, x, y, w, color.rgba, size, h, font)
}

fun NVG.wrappedTextBounds(text: String, width: Float, size: Float, font: Font)
    = renderer.getWrappedStringBounds(context, text, width, size, font)

// TODO: Simplify
enum class TextAlign {
    Left, Middle, Right
}

// TODO: Simplify
enum class TextPos {
    Top, Bottom, Middle
}

val colorCodes = arrayOf(
    Color(0, 0, 0),
    Color(0, 0, 170),
    Color(0, 170, 0),
    Color(0, 170, 170),
    Color(170, 0, 0),
    Color(170, 0, 170),
    Color(255, 170, 0),
    Color(170, 170, 170),
    Color(85, 85, 85),
    Color(85, 85, 255),
    Color(85, 255, 85),
    Color(85, 255, 255),
    Color(255, 85, 85),
    Color(255, 85, 255),
    Color(255, 255, 85),
    Color(255, 255, 255),
    Color(255, 255, 255)
)

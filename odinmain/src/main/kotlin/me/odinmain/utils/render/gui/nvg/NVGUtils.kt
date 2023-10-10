package me.odinmain.utils.render.gui.nvg

import cc.polyfrost.oneconfig.renderer.NanoVGHelper
import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.renderer.scissor.Scissor
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.dsl.nanoVGHelper
import me.odinmain.features.impl.render.ClickGUIModule.experimentalRendering
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.nvg.TextAlign.*
import me.odinmain.utils.render.world.RenderUtils.bindColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Makes it more understanding that [nanoVGHelper] is used for rendering and acts like a wrapper.
 *
 * And helps me cope that one config won`t be removed :sob:
 */
inline val renderer: NanoVGHelper
    get() = nanoVGHelper

private val tessellator: Tessellator = Tessellator.getInstance()
private val worldRenderer: WorldRenderer = tessellator.worldRenderer

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
    if (!experimentalRendering) {
        renderer.drawRoundedRectVaried(context, x, y, w, h, color.rgba, topL, topR, botR, botL)
        return
    }
    GlStateManager.pushMatrix()
    GlStateManager.enableAlpha()
    GlStateManager.disableTexture2D()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    color.bindColor()

    // Draw the top-left corner
    drawCircle(x + topL, y + topL, topL)

    // Draw the top-right corner
    drawCircle(x + w - topR, y + topR, topR)

    // Draw the bottom-left corner
    drawCircle(x + botL, y + h - botL, botL)

    // Draw the bottom-right corner
    drawCircle(x + w - botR, y + h - botR, botR)

    // Draw the top edge
    glRect(x + topL, y, w - topL - topR, topL)

    // Draw the right edge
    glRect(x + w - max(topR, botR), y + topR, max(topR, botR), h - topR - botR)

    // Draw the bottom edge
    glRect(x + botL, y + h - max(botR, botL), w - botL - botR, botL)

    // Draw the left edge
    glRect(x, y + topL, max(topL, botL), h - topL - botR)

    // Draw the middle
    glRect(x + topL, y + topL, w - max(topR, botR), h - max(botL, botR))

    GlStateManager.resetColor()
    GlStateManager.enableTexture2D()
    //GlStateManager.disableBlend()
    GlStateManager.popMatrix()
}

fun NVG.rect(
    x: Float, y: Float, w: Float, h: Float, color: Color, radius: Float = 0f
) = rect(x, y, w, h, color, radius, radius, radius, radius)

fun glRect(x: Float, y: Float, w: Float, h: Float) {
    val pos = mutableListOf(x, y, x + w, y + h)
    if (pos[0] > pos[2])
        Collections.swap(pos, 0, 2)
    if (pos[1] > pos[3])
        Collections.swap(pos, 1, 3)

    GlStateManager.pushMatrix()
    worldRenderer.begin(7, DefaultVertexFormats.POSITION)
    worldRenderer.pos(pos[0].toDouble(), pos[3].toDouble(), 0.0).endVertex()
    worldRenderer.pos(pos[2].toDouble(), pos[3].toDouble(), 0.0).endVertex()
    worldRenderer.pos(pos[2].toDouble(), pos[1].toDouble(), 0.0).endVertex()
    worldRenderer.pos(pos[0].toDouble(), pos[1].toDouble(), 0.0).endVertex()
    tessellator.draw()
    GlStateManager.popMatrix()
}

fun drawCircle(x: Float, y: Float, radius: Float, steps: Int = 20) {
    val theta = 2 * PI / steps
    val cos = cos(theta).toFloat()
    val sin = sin(theta).toFloat()

    var xHolder: Float
    var circleX = 1f
    var circleY = 0f

    GlStateManager.pushMatrix()
    worldRenderer.begin(5, DefaultVertexFormats.POSITION)

    for (i in 0..steps) {
        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
        worldRenderer.pos((circleX * radius + x).toDouble(), (circleY * radius + y).toDouble(), 0.0).endVertex()
        xHolder = circleX
        circleX = cos * circleX - sin * circleY
        circleY = sin * xHolder + cos * circleY
        worldRenderer.pos((circleX * radius + x).toDouble(), (circleY * radius + y).toDouble(), 0.0).endVertex()
    }

    tessellator.draw()
    GlStateManager.popMatrix()
}

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

fun NVG.textWithControlCodes(text: String?, x: Float, y: Float, size: Float, font: Font): Float {
    if (text == null) return 0f
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

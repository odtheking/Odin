package me.odinmain.utils.render.gui.nvg

import gg.essential.universal.UMatrixStack
import me.odinmain.OdinMain.mc
import me.odinmain.ui.util.FontRenderer
import me.odinmain.ui.util.RoundedRect
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.nvg.TextAlign.*
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.image.BufferedImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class Font(val fr: FontRenderer)
object Fonts {
    val REGULAR = Font(FontRenderer("/fonts/Heebo.ttf", 32f))
    val MEDIUM = Font(FontRenderer("/fonts/Heebo.ttf", 70f))
    val SEMIBOLD = Font(FontRenderer("/fonts/Heebo.ttf", 50f))
}



private val tessellator: Tessellator = Tessellator.getInstance()
private val worldRenderer: WorldRenderer = tessellator.worldRenderer

fun rect2Corners(x: Number, y: Number, w: Number, h: Number, color: Color, radius: Float, cornerID: Int) {
    if (color.isTransparent) return
    val sr = ScaledResolution(mc)
    GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)
    val matrix = UMatrixStack.Compat
    matrix.runLegacyMethod(matrix.get()) {
        RoundedRect.drawRoundedRectangle2Corners(
            matrix.get(), x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), radius.toFloat(), color.javaColor, cornerID
        )
    }
    GlStateManager.scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
}

fun rect(
    x: Number, y: Number, w: Number, h: Number, color: Color, radius: Number
) {
    if (color.isTransparent) return
    val sr = ScaledResolution(mc)
    GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)
    val matrix = UMatrixStack.Compat
    matrix.runLegacyMethod(matrix.get()) {
        RoundedRect.drawRoundedRectangle(
            matrix.get(), x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), radius.toFloat(),
            color.javaColor
        )
    }
    GlStateManager.scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
}


fun rect(
    x: Float, y: Float, w: Float, h: Float, color: Color
) = rect(x, y, w, h, color, 0f)

fun rectOutline(x: Float, y: Float, w: Float, h: Float, color: Color, radius: Float = 0f, thickness: Float) {
    if (color.isTransparent) return
    val sr = ScaledResolution(mc)
    GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)
    val matrix = UMatrixStack.Compat
    matrix.runLegacyMethod(matrix.get()) {
        RoundedRect.drawRoundedRectangleOutline(
            matrix.get(), x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), radius.toFloat(),
            color.javaColor, thickness
        )
    }
    GlStateManager.scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)

    /*renderer.drawHollowRoundRect(
        context, x - .95f, y - .95f, w + .5f, h + .5f, color.rgba, radius - .5f, thickness
    )*/
}

fun gradientRect(x: Float, y: Float, w: Float, h: Float, color1: Color, color2: Color, radius: Float) {
    if (color1.isTransparent && color2.isTransparent) return
    //renderer.drawGradientRoundedRect(context, x, y, w, h, color1.rgba, color2.rgba, radius, NanoVGHelper.GradientDirection.RIGHT)
}

fun drawHSBBox(x: Float, y: Float, w: Float, h: Float, color: Color) {
    /*nanoVGHelper.drawHSBBox(context, x, y, w, h, color.rgba)
    rectOutline(x, y, w, h, Color(38, 38, 38), 8f, 1f)

     */
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

fun circle(x: Float, y: Float, radius: Float, color: Color) = Unit

fun textWithControlCodes(text: String?, x: Float, y: Float, size: Float, font: Font): Float {
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
        text(char.toString(), xPos, y, color, size, font, Left, TextPos.Middle)
        xPos += getTextWidth(char.toString(), size, font)
        i++
    }
    return xPos
}

fun text(text: String, x: Float, y: Float, color: Color, size: Float, font: Font, align: TextAlign = Left, verticalAlign: TextPos = TextPos.Middle) {
    if (color.isTransparent) return
    val drawX = when (align) {
        Left -> x
        Right -> x - getTextWidth(text, size, font)
        Middle -> x - getTextWidth(text, size, font) / 2f
    }

    val drawY = when (verticalAlign) {
        TextPos.Top -> y
        TextPos.Middle -> y - getTextHeight(text, size, font) / 2f
        TextPos.Bottom -> y - getTextHeight(text, size, font)
    }

    val sr = ScaledResolution(mc)
    scale(1f / sr.scaleFactor , 1f / sr.scaleFactor, 1f)
    font.fr.drawString(text, drawX, drawY, color)
    scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
}

fun getTextWidth(text: String, size: Float, font: Font) = font.fr.getWidth(text)

fun getTextHeight(text: String, size: Float, font: Font) = font.fr.getHeight(text)

fun dropShadow(x: Float, y: Float, w: Float, h: Float, blur: Float, spread: Float, radius: Float) = Unit
    //renderer.drawDropShadow(context, x, y, w, h, blur, spread, radius)

fun translate(x: Float, y: Float, z: Float = 0f) = GlStateManager.translate(x, y, z)

fun scale(x: Float, y: Float, z: Float = 1f) = GlStateManager.scale(x, y, z)

fun setAlpha(alpha: Float) = Unit
    //renderer.setAlpha(context, alpha)
/*
fun scissor(x: Float, y: Float, w: Float, h: Float): Any? {

    //return ScissorHelper.INSTANCE.scissor(context, x, y, w, h)
    return null
}

fun resetScissor(scissor: Scissor) {
    ScissorHelper.INSTANCE.resetScissor(context, scissor)
}*/



fun image(filePath: String, x: Float, y: Float, w: Float, h: Float) {
    mc.textureManager.bindTexture(ResourceLocation("odinclient", filePath))
    drawTexturedModalRect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
}

fun drawGLTexture(texture: Int, x: Number, y: Number, w: Number, h: Number) {
    GlStateManager.bindTexture(texture)
    drawTexturedModalRect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
}

fun drawBufferedImage(dynamicTexture: DynamicTexture, x: Float, y: Float, w: Float, h: Float) {
    dynamicTexture.updateDynamicTexture()
    GlStateManager.bindTexture(dynamicTexture.glTextureId)
    //mc.textureManager.bindTexture(mc.textureManager.getDynamicTextureLocation("temporary_stuff", dynamicTexture))
    val sr = ScaledResolution(mc)
    scale(1f / sr.scaleFactor , 1f / sr.scaleFactor, 1f)
    drawTexturedModalRect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
}

fun wrappedText(text: String, x: Float, y: Float, w: Float, h: Float, color: Color, size: Float, font: Font) {
    if (color.isTransparent) return

    //renderer.drawWrappedString(context, text, x, y, w, color.rgba, size, h, font)
}

fun wrappedTextBounds(text: String, width: Float, size: Float, font: Font) = Unit
    //renderer.getWrappedStringBounds(context, text, width, size, font)

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

fun drawTexturedModalRect(x: Int, y: Int, width: Int, height: Int) {
    GlStateManager.enableTexture2D()
    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
    worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(0.0, 1.0).endVertex()
    worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(1.0, 1.0).endVertex()
    worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
    worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
    tessellator.draw()
}

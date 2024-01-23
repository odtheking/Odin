package me.odinmain.utils.render.gui

import gg.essential.universal.UMatrixStack
import me.odinmain.OdinMain.mc
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.util.FontRenderer
import me.odinmain.ui.util.RoundedRect
import me.odinmain.utils.coerceAlpha
import me.odinmain.utils.loadBufferedImage
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.TextAlign.*
import me.odinmain.utils.render.world.RenderUtils.drawTexturedModalRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture


class Font(val fr: FontRenderer)
object Fonts {
    val REGULAR = Font(FontRenderer("/fonts/Heebo.ttf", 32f))
    val MEDIUM = Font(FontRenderer("/fonts/Heebo.ttf", 70f))
    val SEMIBOLD = Font(FontRenderer("/fonts/Heebo.ttf", 50f))
}

val matrix = UMatrixStack.Compat
val sr = ScaledResolution(mc)

fun roundedRectangle(
    x: Number, y: Number, w: Number, h: Number,
    color: Color, borderColor: Color, shadowColor: Color,
    borderThickness: Number, topL: Number, topR: Number, botL: Number, botR: Number, edgeSoftness: Number,
    color2: Color = color, gradientDir: Int = 0, shadowSoftness: Float = 0f
) {
    scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)
    matrix.runLegacyMethod(matrix.get()) {
        RoundedRect.drawRectangle(
            matrix.get(), x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(),
            color, borderColor, shadowColor, borderThickness.toFloat(), topL.toFloat(), topR.toFloat(), botL.toFloat(), botR.toFloat(), edgeSoftness.toFloat(), color2, gradientDir, shadowSoftness
        )
    }
    scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
}

fun roundedRectangle(x: Number, y: Number, w: Number, h: Number, color: Color, radius: Number = 0f) =
    roundedRectangle(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color, color, color,
        0f, radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), 0f)


fun rectangleOutline(x: Float, y: Float, w: Float, h: Float, color: Color, radius: Float = 0f, thickness: Float) {
    roundedRectangle(x, y, w, h, Color.TRANSPARENT, color, Color.GRAY, thickness, radius, radius, radius, radius, 0f)
}

enum class GradientDirection {
    Right, Down, Left, Up
}

fun gradientRect(x: Float, y: Float, w: Float, h: Float, color1: Color, color2: Color, radius: Float, direction: GradientDirection = GradientDirection.Right) {
    if (color1.isTransparent && color2.isTransparent) return
    roundedRectangle(
        x, y, w, h, color1.coerceAlpha(.1f, 1f), Color.TRANSPARENT, Color.TRANSPARENT, 0, radius, radius, radius, radius, 3, color2.coerceAlpha(.1f, 1f), direction.ordinal
    )
}

fun drawHSBBox(x: Float, y: Float, w: Float, h: Float, color: Color) {
    /*nanoVGHelper.drawHSBBox(context, x, y, w, h, color.rgba)
    rectOutline(x, y, w, h, Color(38, 38, 38), 8f, 1f)

     */
}

fun circle(x: Float, y: Float, radius: Float, color: Color) = Unit

fun text(text: String, x: Float, y: Float, color: Color, size: Float, font: Font, align: TextAlign = Left, verticalAlign: TextPos = TextPos.Middle, shadow: Boolean = false) {
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

    scale(1f / sr.scaleFactor , 1f / sr.scaleFactor, 1f)
    if (shadow) font.fr.drawStringWithShadow(text, drawX, drawY, color)
    else font.fr.drawString(text, drawX, drawY, color)
    scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
}

fun textWithControlCodes(text: String?, x: Float, y: Float, color: Color = Color.WHITE, size: Float, font: Font, align: TextAlign = Left, verticalAlign: TextPos = TextPos.Middle, shadow: Boolean = false): Float {
    if (text == null) return 0f
    var i = 0
    var textColor = color
    var xPos = x
    while (i < text.length) {
        val char = text[i]
        if (char == '\u00a7' && i + 1 < text.length) {
            val colorCode = "0123456789abcdefr".indexOf(text.lowercase()[i + 1])
            textColor = colorCodes[colorCode]

            i += 2
            continue
        }
        text(char.toString(), xPos, y, textColor, size, font, align, verticalAlign, shadow)
        xPos += getTextWidth(char.toString(), size, font)
        i++
    }
    return xPos
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

fun drawBufferedImage(filePath: String, x: Float, y: Float, w: Float, h: Float) {
    val image = loadBufferedImage(filePath)
    val dynamicTexture = DynamicTexture(image)
    dynamicTexture.updateDynamicTexture()
    GlStateManager.bindTexture(dynamicTexture.glTextureId)
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



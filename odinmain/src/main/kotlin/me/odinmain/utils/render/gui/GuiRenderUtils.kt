package me.odinmain.utils.render.gui

import gg.essential.universal.UMatrixStack
import me.odinmain.OdinMain.mc
import me.odinmain.ui.util.FontRenderer
import me.odinmain.ui.util.RoundedRect
import me.odinmain.utils.coerceAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.TextAlign.*
import me.odinmain.utils.render.world.RenderUtils.drawTexturedModalRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import org.lwjgl.opengl.GL11
import kotlin.math.max


class Font(val fr: FontRenderer)
object Fonts {
    val REGULAR = Font(FontRenderer("/assets/odinmain/fonts/Regular.ttf", 32f))
    val MEDIUM = Font(FontRenderer("/assets/odinmain/fonts/Medium.ttf", 50f))
    val SEMIBOLD = Font(FontRenderer("/assets/odinmain/fonts/SemiBold.ttf", 50f))
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

fun roundedRectangle(x: Number, y: Number, w: Number, h: Number, color: Color, radius: Number = 0f, edgeSoftness: Number = 0.5f) =
    roundedRectangle(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color, color, color,
        0f, radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), edgeSoftness)


fun rectangleOutline(x: Float, y: Float, w: Float, h: Float, color: Color, radius: Float = 0f, thickness: Float) {
    roundedRectangle(x, y, w, h, Color.TRANSPARENT, color, Color.TRANSPARENT, thickness, radius, radius, radius, radius, 1f)
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
    scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)
    matrix.runLegacyMethod(matrix.get()) { RoundedRect.drawHSBBox(matrix.get(), x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color,) }
    scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
    rectangleOutline(x-1, y-1, w+2, h+2, Color(38, 38, 38), 3f, 2f)
}

fun circle(x: Number, y: Number, radius: Number, color: Color, borderColor: Color = color, borderThickness: Number = 0f) {
    scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)
    matrix.runLegacyMethod(matrix.get()) { RoundedRect.drawCircle(matrix.get(), x.toFloat(), y.toFloat(), radius.toFloat(), color, borderColor, borderThickness.toFloat()) }
    scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
}


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

fun getTextWidth(text: String, size: Float, font: Font) = font.fr.getWidth(text)

fun getTextHeight(text: String, size: Float, font: Font) = font.fr.getHeight(text)

fun translate(x: Float, y: Float, z: Float = 0f) = GlStateManager.translate(x, y, z)

fun scale(x: Float, y: Float, z: Float = 1f) = GlStateManager.scale(x, y, z)

fun dropShadow(x: Float, y: Float, w: Float, h: Float, blur: Float, spread: Float, radius: Float) = Unit//roundedRectangle(x, y, w, h, Color.DARK_GRAY, radius, spread)
//renderer.drawDropShadow(context, x, y, w, h, blur, spread, radius)

fun setAlpha(alpha: Float) = Unit
//renderer.setAlpha(context, alpha)

data class Scissor(val x: Number, val y: Number, val w: Number, val h: Number, val context: Int)
private val scissorList = mutableListOf<Scissor>(Scissor(0, 0, 4000, 4000, 0))

fun scissor(x: Number, y: Number, w: Number, h: Number): Scissor {
    GL11.glScissor(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    val scissor = Scissor(x, y, w, h, scissorList.size)
    scissorList.add(scissor)
    return scissor
}

fun resetScissor(scissor: Scissor) {
    val nextScissor = scissorList[scissor.context - 1]
    GL11.glScissor(nextScissor.x.toInt(), nextScissor.y.toInt(), nextScissor.w.toInt(), nextScissor.h.toInt())
    scissorList.removeLast()
}

fun drawDynamicTexture(dynamicTexture: DynamicTexture, x: Float, y: Float, w: Float, h: Float) {
    dynamicTexture.updateDynamicTexture()
    GlStateManager.bindTexture(dynamicTexture.glTextureId)
    scale(1f / sr.scaleFactor , 1f / sr.scaleFactor, 1f)
    drawTexturedModalRect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    scale(sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat(), 1f)
}

fun wrappedText(text: String, x: Float, y: Float, w: Float, h: Float, color: Color, size: Float, font: Font) {
    if (color.isTransparent) return

    val words = text.split(" ")
    var line = ""
    var currentHeight = y

    for (word in words) {
        if (font.fr.getWidth(line + word) > w) {
            text(line, x, currentHeight, color, size, font)
            line = "$word "
            currentHeight += font.fr.getHeight(line)
        }
        else line += "$word "

    }
    text(line, x, currentHeight, color, size, font)
}

fun wrappedTextBounds(text: String, width: Float, size: Float, font: Font): Pair<Float, Float> {
    val words = text.split(" ")
    var line = ""
    var lines = 1
    var maxWidth = 0f

    for (word in words) {
        if (font.fr.getWidth(line + word) > width) {
            maxWidth = max(maxWidth, font.fr.getWidth(line))
            line = "$word "
            lines++
        }
        else line += "$word "

    }
    maxWidth = max(maxWidth, font.fr.getWidth(line))

    return Pair(maxWidth, lines * font.fr.getHeight(line))
}

enum class TextAlign {
    Left, Middle, Right
}
enum class TextPos {
    Top, Bottom, Middle
}
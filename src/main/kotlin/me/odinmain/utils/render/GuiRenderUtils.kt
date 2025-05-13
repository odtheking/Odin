package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.font.OdinFont
import me.odinmain.utils.div
import me.odinmain.utils.minus
import me.odinmain.utils.plus
import me.odinmain.utils.render.RenderUtils.drawTexturedModalRect
import me.odinmain.utils.render.TextAlign.Left
import me.odinmain.utils.times
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.ui.shader.CircleShader
import me.odinmain.utils.ui.shader.DropShadowShader
import me.odinmain.utils.ui.shader.HSBBoxShader
import me.odinmain.utils.ui.shader.RoundedRectangleShader
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11

val scaleFactor get() = ScaledResolution(mc).scaleFactor.toFloat()

data class Box(var x: Number, var y: Number, var w: Number, var h: Number)
data class BoxWithClass<T : Number>(var x: T, var y: T, var w: T, var h: T)
fun Box.expand(factor: Number): Box = Box(this.x - factor, this.y - factor, this.w + factor * 2, this.h + factor * 2)
fun Box.isPointWithin(x: Number, y: Number): Boolean {
    return x.toDouble() >= this.x.toDouble() &&
            y.toDouble() >= this.y.toDouble() &&
            x.toDouble() <= (this.x.toDouble() + this.w.toDouble()) &&
            y.toDouble() <= (this.y.toDouble() + this.h.toDouble())
}

fun roundedRectangle(
    x: Number, y: Number, w: Number, h: Number,
    color: Color, borderColor: Color, shadowColor: Color,
    borderThickness: Number, topL: Number, topR: Number, botL: Number, botR: Number, edgeSoftness: Number,
    color2: Color = color, gradientDir: Int = 0, shadowSoftness: Float = 0f
) {
    RoundedRectangleShader.drawRectangle(
        x.toFloat(),
        y.toFloat(),
        w.toFloat(),
        h.toFloat(),
        color,
        borderColor,
        shadowColor,
        borderThickness.toFloat(),
        topL.toFloat(),
        topR.toFloat(),
        botL.toFloat(),
        botR.toFloat(),
        edgeSoftness.toFloat(),
        color2,
        gradientDir,
        shadowSoftness
    )
}

fun roundedRectangle(x: Number, y: Number, w: Number, h: Number, color: Color, radius: Number = 0f, edgeSoftness: Number = 0.5f) =
    roundedRectangle(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color, color, color,
        0f, radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), edgeSoftness)

fun roundedRectangle(box: Box, color: Color, radius: Number = 0f, edgeSoftness: Number = .5f) =
    roundedRectangle(box.x, box.y, box.w, box.h, color, radius, edgeSoftness)

fun <T: Number> roundedRectangle(box: BoxWithClass<T>, color: Color, radius: Number = 0f, edgeSoftness: Number = .5f) =
    roundedRectangle(box.x, box.y, box.w, box.h, color, radius, edgeSoftness)


fun rectangleOutline(x: Number, y: Number, w: Number, h: Number, color: Color, radius: Number = 0f, thickness: Number, edgeSoftness: Number = 1f) {
    roundedRectangle(x, y, w, h, Colors.TRANSPARENT, color, Colors.TRANSPARENT, thickness, radius, radius, radius, radius, edgeSoftness)
}

fun gradientRect(x: Float, y: Float, w: Float, h: Float, color1: Color, color2: Color, radius: Float, direction: GradientDirection = GradientDirection.Right, borderColor: Color = Colors.TRANSPARENT, borderThickness: Number = 0f) {
    if (color1.isTransparent && color2.isTransparent) return
    roundedRectangle(
        x, y, w, h, color1.coerceAlpha(.1f, 1f), borderColor, Colors.TRANSPARENT, borderThickness, radius, radius, radius, radius, 3, color2.coerceAlpha(.1f, 1f), direction.ordinal
    )
}

fun drawHSBBox(x: Float, y: Float, w: Float, h: Float, color: Color) {
    HSBBoxShader.drawHSBBox(x, y, w, h, color)
    rectangleOutline(x-1, y-1, w+2, h+2, Color(38, 38, 38), 3f, 2f)
}

fun circle(x: Number, y: Number, radius: Number, color: Color, borderColor: Color = color, borderThickness: Number = 0f) {
    CircleShader.drawCircle(
        x.toFloat(),
        y.toFloat(),
        radius.toFloat(),
        color,
        borderColor,
        borderThickness.toFloat()
    )
}

fun text(text: String, x: Number, y: Number, color: Color, size: Number, type: Int = OdinFont.REGULAR, align: TextAlign = Left, verticalAlign: TextPos = TextPos.Middle, shadow: Boolean = false) {
    OdinFont.text(text, x.toFloat(), y.toFloat(), color, size.toFloat(), align, verticalAlign, shadow, type)
}

fun mcText(text: String, x: Number, y: Number, scale: Number, color: Color, shadow: Boolean = true, center: Boolean = true) {
    RenderUtils.drawText("$text§r", x.toFloat(), y.toFloat(), scale.toFloat(), color, shadow, center)
}

fun mcTextAndWidth(text: String, x: Number, y: Number, scale: Number, color: Color, shadow: Boolean = true, center: Boolean = true): Float {
    mcText(text, x, y, scale, color, shadow, center)
    return getMCTextWidth(text).toFloat()
}

fun getMCTextWidth(text: String) = mc.fontRendererObj.getStringWidth(text)

fun getTextWidth(text: String, size: Float) = OdinFont.getTextWidth(text, size)

fun getMCTextHeight() = mc.fontRendererObj.FONT_HEIGHT

fun rotate(degrees: Float, xPos: Float, yPos: Float, zPos: Float, xAxis: Float, yAxis: Float, zAxis: Float) {
    GlStateManager.translate(xPos, yPos, zPos)
    GlStateManager.rotate(degrees, xAxis, yAxis, zAxis)
    GlStateManager.translate(-xPos, -yPos, -zPos)
}

fun dropShadow(x: Number, y: Number, w: Number, h: Number, shadowColor: Color, shadowSoftness: Number, topL: Number, topR: Number, botL: Number, botR: Number) {
    GlStateManager.translate(0f, 0f, -100f)

    DropShadowShader.drawShadow(
        (x - shadowSoftness / 2).toFloat(),
        (y - shadowSoftness / 2).toFloat(),
        (w + shadowSoftness).toFloat(),
        (h + shadowSoftness).toFloat(),
        shadowColor,
        topL.toFloat(),
        topR.toFloat(),
        botL.toFloat(),
        botR.toFloat(),
        shadowSoftness.toFloat()
    )

    GlStateManager.translate(0f, 0f, 100f)
}

fun dropShadow(x: Number, y: Number, w: Number, h: Number,  radius: Number, shadowSoftness: Number = 1f, shadowColor: Color = ColorUtil.moduleButtonColor) {
    dropShadow(x, y, w, h, shadowColor, shadowSoftness, radius, radius, radius, radius)
}

fun dropShadow(box: Box, radius: Number, shadowSoftness: Number = 1f, shadowColor: Color = ColorUtil.moduleButtonColor) =
    dropShadow(box.x, box.y, box.w, box.h, radius, shadowSoftness, shadowColor)

data class Scissor(val x: Number, val y: Number, val w: Number, val h: Number, val context: Int)
private val scissorList = mutableListOf(Scissor(0, 0, 16000, 16000, 0))

fun scissor(x: Number, y: Number, w: Number, h: Number): Scissor {
    GL11.glEnable(GL11.GL_SCISSOR_TEST)
    GL11.glScissor(x.toInt(), Display.getHeight() - y.toInt() - h.toInt(), w.toInt(), h.toInt())
    val scissor = Scissor(x, y, w, h, scissorList.size)
    scissorList.add(scissor)
    return scissor
}

fun resetScissor(scissor: Scissor) {
    val nextScissor = scissorList[scissor.context - 1]
    GL11.glScissor(nextScissor.x.toInt(), nextScissor.y.toInt(), nextScissor.w.toInt(), nextScissor.h.toInt())
    GL11.glDisable(GL11.GL_SCISSOR_TEST)
    scissorList.removeLast()
}

fun drawDynamicTexture(dynamicTexture: DynamicTexture, x: Number, y: Number, w: Number, h: Number) {
    dynamicTexture.updateDynamicTexture()
    GlStateManager.bindTexture(dynamicTexture.glTextureId)
    drawTexturedModalRect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
}

fun wrappedText(text: String, x: Float, y: Float, w: Float, color: Color, size: Float, type: Int = OdinFont.REGULAR, shadow: Boolean = false) {
    OdinFont.wrappedText(text, x, y, w, color, size, type, shadow = shadow)
}

fun wrappedTextBounds(text: String, width: Float, size: Float): Pair<Float, Float> {
    return OdinFont.wrappedTextBounds(text, width, size)
}

enum class TextAlign {
    Left, Middle, Right
}
enum class TextPos {
    Top, Bottom, Middle
}

enum class GradientDirection {
    Right, Down, Left, Up
}

fun Color.coerceAlpha(min: Float, max: Float): Color {
    return if (this.alphaFloat < min) this.withAlpha(min)
    else if (this.alphaFloat > max) this.withAlpha(max)
    else this
}

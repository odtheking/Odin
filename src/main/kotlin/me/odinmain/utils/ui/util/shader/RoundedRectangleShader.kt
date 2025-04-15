package me.odinmain.utils.ui.util.shader

import me.odinmain.utils.render.Color
import net.minecraft.client.gui.Gui

object RoundedRectangleShader: Shader("/shaders/rectangle.vsh", "/shaders/roundedrectangle.fsh") {
    private val directionVecs = listOf(Pair(1f, 0f), Pair(0f, 1f), Pair(-1f, 0f), Pair(0f, -1f))

    fun drawRectangle(
        x: Float, y: Float, width: Float, height: Float,
        color: Color, borderColor: Color, shadowColor: Color,
        borderThickness: Float, topL: Float, topR: Float, botL: Float, botR: Float, edgeSoftness: Float,
        color2: Color, gradientDir: Int, shadowSoftness: Float = 0f
    ) {
        if (!usable) return

        bind()

        getFloat2Uniform("u_rectCenter").setValue(x + (width / 2), y + (height / 2))
        getFloat2Uniform("u_rectSize").setValue(width, height)
        getFloat4Uniform("u_Radii").setValue(botR, topR, botL, topL)
        getFloatUniform("u_borderThickness").setValue(borderThickness)
        getFloatUniform("u_edgeSoftness").setValue(edgeSoftness)
        getFloat4Uniform("u_colorRect").setValue(color.red / 255f, color.green / 255f, color.blue / 255f, color.alphaFloat)
        getFloat4Uniform("u_colorRect2").setValue(color2.red / 255f, color2.green / 255f, color2.blue / 255f, color2.alphaFloat)
        getFloat2Uniform("u_gradientDirectionVector").setValue(directionVecs[gradientDir].first, directionVecs[gradientDir].second)
        getFloat4Uniform("u_colorBorder").setValue(borderColor.red / 255f, borderColor.green / 255f, borderColor.blue / 255f, borderColor.alphaFloat)
        getFloat4Uniform("u_colorShadow").setValue(shadowColor.red / 255f, shadowColor.green / 255f, shadowColor.blue / 255f, shadowColor.alphaFloat)
        getFloatUniform("u_shadowSoftness").setValue(shadowSoftness)

        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), color.rgba)

        unbind()
    }
}
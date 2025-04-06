package me.odinmain.ui.util.shader

import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils

object RoundedRectangleShader: Shader("/shaders/source/rectangle.vsh", "/shaders/source/roundedrectangle.fsh") {
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
        getFloat4Uniform("u_colorRect").setValue(color.r / 255f, color.g / 255f, color.b / 255f, color.alpha)
        getFloat4Uniform("u_colorRect2").setValue(color2.r / 255f, color2.g / 255f, color2.b / 255f, color2.alpha)
        getFloat2Uniform("u_gradientDirectionVector").setValue(directionVecs[gradientDir].first, directionVecs[gradientDir].second)
        getFloat4Uniform("u_colorBorder").setValue(borderColor.r / 255f, borderColor.g / 255f, borderColor.b / 255f, borderColor.alpha)
        getFloat4Uniform("u_colorShadow").setValue(shadowColor.r / 255f, shadowColor.g / 255f, shadowColor.b / 255f, shadowColor.alpha)
        getFloatUniform("u_shadowSoftness").setValue(shadowSoftness)

        RenderUtils.drawRectangle(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), color)

        unbind()
    }
}
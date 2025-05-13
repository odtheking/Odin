package me.odinmain.utils.ui.shader

import me.odinmain.utils.render.Color
import net.minecraft.client.gui.Gui

object DropShadowShader: Shader("/shaders/rectangle.vsh", "/shaders/dropShadow.fsh") {

    fun drawShadow(
        x: Float, y: Float, width: Float, height: Float,
        shadowColor: Color, topL: Float, topR: Float, botL: Float, botR: Float, shadowSoftness: Float
    ) {
        if (!usable) return

        bind()

        getFloat2Uniform("u_rectCenter").setValue(x + (width / 2), y + (height / 2))
        getFloat2Uniform("u_rectSize").setValue(width, height)
        getFloat4Uniform("u_Radii").setValue(botR, topR, botL, topL)
        getFloat4Uniform("u_colorShadow").setValue(shadowColor.red / 255f, shadowColor.green / 255f, shadowColor.blue / 255f, shadowColor.alphaFloat)
        getFloatUniform("u_shadowSoftness").setValue(shadowSoftness)

        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), shadowColor.rgba)

        unbind()
    }
}
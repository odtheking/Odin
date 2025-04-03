package me.odinmain.ui.util.shader

import me.odinmain.utils.render.Color
import net.minecraft.client.gui.Gui

object CircleShader: Shader("/shaders/source/rectangle.vsh", "/shaders/source/circleFragment.fsh") {

    fun drawCircle(
        x: Float, y: Float, radius: Float,
        color: Color, borderColor: Color, borderThickness: Float
    ) {
        if (!usable) return

        bind()

        getFloat2Uniform("u_circleCenter").setValue(x, y)
        getFloatUniform("u_circleRadius").setValue(radius)
        getFloat4Uniform("u_colorCircle").setValue(color.r / 255f, color.g / 255f, color.b / 255f, color.alpha)
        getFloat4Uniform("u_colorBorder").setValue(borderColor.r / 255f, borderColor.g / 255f, borderColor.b / 255f, borderColor.alpha)
        getFloatUniform("u_borderThickness").setValue(borderThickness)

        Gui.drawRect((x - radius).toInt(), (y - radius).toInt(), (x + radius).toInt(), (y + radius).toInt(), color.rgba)

        unbind()
    }
}
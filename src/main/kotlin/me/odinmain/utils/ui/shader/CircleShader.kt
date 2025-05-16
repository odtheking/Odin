package me.odinmain.utils.ui.shader

import me.odinmain.utils.render.Color
import net.minecraft.client.gui.Gui

object CircleShader: Shader("/shaders/rectangle.vsh", "/shaders/circleFragment.fsh") {

    fun drawCircle(
        x: Float, y: Float, radius: Float,
        color: Color, borderColor: Color, borderThickness: Float
    ) {
        if (!usable) return

        bind()

        getFloat2Uniform("u_circleCenter").setValue(x, y)
        getFloatUniform("u_circleRadius").setValue(radius)
        getFloat4Uniform("u_colorCircle").setValue(color.red / 255f, color.green / 255f, color.blue / 255f, color.alphaFloat)
        getFloat4Uniform("u_colorBorder").setValue(borderColor.red / 255f, borderColor.green / 255f, borderColor.blue / 255f, borderColor.alphaFloat)
        getFloatUniform("u_borderThickness").setValue(borderThickness)

        Gui.drawRect((x - radius).toInt(), (y - radius).toInt(), (x + radius).toInt(), (y + radius).toInt(), color.rgba)

        unbind()
    }
}
package me.odinmain.utils.ui.shader

import me.odinmain.utils.render.Color
import me.odinmain.utils.ui.Colors
import net.minecraft.client.gui.Gui

object CircleShader: Shader("/shaders/rectangle.vsh", "/shaders/circleFragment.fsh") {

    private var x = 0f
    private var y = 0f
    private var radius = 0f
    private var color = Colors.WHITE
    private var borderColor = Colors.WHITE
    private var borderThickness = 0f

    fun drawCircle(
        x: Float, y: Float, radius: Float,
        color: Color, borderColor: Color, borderThickness: Float
    ) {
        if (!usable) return

        this.x = x
        this.y = y
        this.radius = radius
        this.color = color
        this.borderColor = borderColor
        this.borderThickness = borderThickness

        startShader()

        Gui.drawRect((x - radius).toInt(), (y - radius).toInt(), (x + radius).toInt(), (y + radius).toInt(), color.rgba)

        stopShader()
    }

    override fun setupUniforms() {
        setupUniform("u_circleCenter")
        setupUniform("u_circleRadius")
        setupUniform("u_colorCircle")
        setupUniform("u_colorBorder")
        setupUniform("u_borderThickness")
    }

    override fun updateUniforms() {
        getFloat2Uniform("u_circleCenter").setValue(x, y)
        getFloatUniform("u_circleRadius").setValue(radius)
        getFloat4Uniform("u_colorCircle").setValue(color.red / 255f, color.green / 255f, color.blue / 255f, color.alphaFloat)
        getFloat4Uniform("u_colorBorder").setValue(borderColor.red / 255f, borderColor.green / 255f, borderColor.blue / 255f, borderColor.alphaFloat)
        getFloatUniform("u_borderThickness").setValue(borderThickness)
    }
}
package me.odinmain.utils.ui.shader

import me.odinmain.utils.render.Color
import me.odinmain.utils.ui.Colors
import net.minecraft.client.gui.Gui

object RoundedRectangleShader: Shader("/shaders/rectangle.vsh", "/shaders/roundedrectangle.fsh") {

    private val directionVecs = listOf(Pair(1f, 0f), Pair(0f, 1f), Pair(-1f, 0f), Pair(0f, -1f))
    private var x = 0f
    private var y = 0f
    private var width = 0f
    private var height = 0f
    private var color = Colors.WHITE
    private var borderColor = Colors.WHITE
    private var shadowColor = Colors.WHITE
    private var borderThickness = 0f
    private var topL = 0f
    private var topR = 0f
    private var botL = 0f
    private var botR = 0f
    private var edgeSoftness = 0f
    private var color2 = Colors.WHITE
    private var gradientDir = 0
    private var shadowSoftness = 0f


    fun drawRectangle(
        x: Float, y: Float, width: Float, height: Float,
        color: Color, borderColor: Color, shadowColor: Color,
        borderThickness: Float, topL: Float, topR: Float, botL: Float, botR: Float, edgeSoftness: Float,
        color2: Color, gradientDir: Int, shadowSoftness: Float = 0f
    ) {
        if (!usable) return

        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.color = color
        this.borderColor = borderColor
        this.shadowColor = shadowColor
        this.borderThickness = borderThickness
        this.topL = topL
        this.topR = topR
        this.botL = botL
        this.botR = botR
        this.edgeSoftness = edgeSoftness
        this.color2 = color2
        this.gradientDir = gradientDir
        this.shadowSoftness = shadowSoftness

        startShader()

        Gui.drawRect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt(), color.rgba)

        stopShader()
    }

    override fun setupUniforms() {
        setupUniform("u_rectCenter")
        setupUniform("u_rectSize")
        setupUniform("u_Radii")
        setupUniform("u_borderThickness")
        setupUniform("u_edgeSoftness")
        setupUniform("u_colorRect")
        setupUniform("u_colorRect2")
        setupUniform("u_gradientDirectionVector")
        setupUniform("u_colorBorder")
        setupUniform("u_colorShadow")
        setupUniform("u_shadowSoftness")
    }

    override fun updateUniforms() {
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
    }
}
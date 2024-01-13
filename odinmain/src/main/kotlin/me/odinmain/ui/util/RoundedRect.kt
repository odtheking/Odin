package me.odinmain.ui.util

import gg.essential.elementa.components.UIBlock
import gg.essential.universal.UMatrixStack
import gg.essential.universal.shader.*
import me.odinmain.utils.createLegacyShader
import java.awt.Color

object RoundedRect {
    private lateinit var shader: UShader
    private lateinit var shaderRadiusUniform: FloatUniform
    private lateinit var shaderInnerRectUniform: Float4Uniform

    private lateinit var shader2corners: UShader
    private lateinit var shaderRadiusUniform2corners: FloatUniform
    private lateinit var shaderInnerRectUniform2corners: Float4Uniform
    private lateinit var shaderCornerIDsUniform2corners: IntUniform

    fun initShaders() {
        initShader()
        initShader2corners()
    }

    private fun initShader() {
        if (::shader.isInitialized)
            return

        shader = createLegacyShader("rectangle", "roundedRectangle", BlendState.NORMAL)
        if (!shader.usable) {
            println("Failed to load Odin rounded rectangle shader")
            return
        }
        shaderRadiusUniform = shader.getFloatUniform("u_Radius")
        shaderInnerRectUniform = shader.getFloat4Uniform("u_InnerRect")
        println("Loaded Odin rounded rectangle shader")
    }

    private fun initShader2corners() {
        if (::shader2corners.isInitialized) return

        shader2corners = createLegacyShader("rectangle", "roundedRectangle2Corners", BlendState.NORMAL)
        if (!shader2corners.usable) {
            println("Failed to load Odin rounded rectangle (2 corners) shader")
            return
        }
        shaderRadiusUniform2corners = shader2corners.getFloatUniform("u_Radius")
        shaderInnerRectUniform2corners = shader2corners.getFloat4Uniform("u_InnerRect")
        shaderCornerIDsUniform2corners = shader2corners.getIntUniform("u_CornerID")
        println("Loaded Odin rounded rectangle (2 corners) shader")
    }

    /**
     * Draws a rounded rectangle
     */
    fun drawRoundedRectangle(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        if (!::shader.isInitialized || !shader.usable)
            return

        shader.bind()
        shaderRadiusUniform.setValue(radius)
        shaderInnerRectUniform.setValue(x + radius, y + radius, x + width - radius, y + height - radius)

        UIBlock.drawBlockWithActiveShader(matrixStack, color, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        shader.unbind()
    }

    fun drawRoundedRectangle2Corners(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color, cornerID: Int) {
        if (!::shader2corners.isInitialized || !shader2corners.usable) return

        shader2corners.bind()
        shaderRadiusUniform2corners.setValue(radius)
        shaderInnerRectUniform2corners.setValue(x + radius, y + radius, x + width - radius, y + height - radius)
        shaderCornerIDsUniform2corners.setValue(cornerID)

        UIBlock.drawBlockWithActiveShader(matrixStack, color, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        shader2corners.unbind()
    }
}
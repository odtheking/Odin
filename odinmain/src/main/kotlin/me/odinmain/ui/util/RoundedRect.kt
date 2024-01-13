package me.odinmain.ui.util

import gg.essential.elementa.components.UIBlock
import gg.essential.universal.UMatrixStack
import gg.essential.universal.shader.BlendState
import gg.essential.universal.shader.Float2Uniform
import gg.essential.universal.shader.Float4Uniform
import gg.essential.universal.shader.UShader
import me.odinmain.utils.createLegacyShader
import java.awt.Color

object RoundedRect {
    private lateinit var shader: UShader
    private lateinit var shaderRadiusUniform: Float4Uniform
    private lateinit var shaderSizeUniform: Float2Uniform
    private lateinit var shaderLocationUniform: Float2Uniform

    fun initShaders() {
        if (::shader.isInitialized)
            return

        shader = createLegacyShader("rectangle", "roundedRectangle", BlendState.NORMAL)
        if (!shader.usable) {
            println("Failed to load Odin rounded rectangle shader")
            return
        }
        shaderRadiusUniform = shader.getFloat4Uniform("u_Radius")
        shaderSizeUniform = shader.getFloat2Uniform("u_Size")
        shaderLocationUniform = shader.getFloat2Uniform("u_Location")
        println("Loaded Odin rounded rectangle shader")
    }

    /**
     * Draws a rounded rectangle
     */
    fun drawRoundedRectangle(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, topL: Float, topR: Float, botL: Float, botR: Float, color: Color) {
        if (!::shader.isInitialized || !shader.usable)
            return

        shader.bind()
        shaderRadiusUniform.setValue(botR, topR, botL, topL)
        shaderSizeUniform.setValue(width, height);
        shaderLocationUniform.setValue(x, y)

        UIBlock.drawBlockWithActiveShader(matrixStack, color, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        shader.unbind()
    }
}
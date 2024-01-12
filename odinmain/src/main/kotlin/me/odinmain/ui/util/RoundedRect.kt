package me.odinmain.ui.util

import gg.essential.elementa.components.UIBlock
import gg.essential.universal.UMatrixStack
import gg.essential.universal.shader.BlendState
import gg.essential.universal.shader.Float4Uniform
import gg.essential.universal.shader.FloatUniform
import gg.essential.universal.shader.UShader
import me.odinmain.utils.createLegacyShader
import java.awt.Color

object RoundedRect {
    private lateinit var shader: UShader
    private lateinit var shaderRadius: Float4Uniform
    //private lateinit var shaderRadius: FloatUniform
    private lateinit var shaderInnerRectUniform: Float4Uniform

    fun initShaders() {
        if (::shader.isInitialized)
            return

        shader = createLegacyShader("rect", "rounded_rect", BlendState.NORMAL)
        if (!shader.usable) {
            println("Failed to load Odin rounded rectangle shader")
            return
        }
        shaderRadius = shader.getFloat4Uniform("u_Radius")
        shaderInnerRectUniform = shader.getFloat4Uniform("u_InnerRect")
        println("Loaded Odin rounded rectangle shader")
    }

    /**
     * Draws a rounded rectangle
     */
    fun drawRoundedRectangle(matrixStack: UMatrixStack, left: Float, top: Float, right: Float, bottom: Float, topL: Float, topR: Float, botL: Float, botR: Float, color: Color) {
        if (!::shader.isInitialized || !shader.usable)
            return

        shader.bind()
        shaderRadius.setValue(topL, topR, botL, botR)
        shaderInnerRectUniform.setValue(left + topL, top + topL, right - botR, bottom - botR)

        UIBlock.drawBlockWithActiveShader(matrixStack, color, left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble())

        shader.unbind()
    }
}
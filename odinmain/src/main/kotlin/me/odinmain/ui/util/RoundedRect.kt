package me.odinmain.ui.util

import gg.essential.elementa.components.UIBlock
import gg.essential.universal.UMatrixStack
import gg.essential.universal.shader.*
import me.odinmain.utils.createLegacyShader
import java.awt.Color

object RoundedRect {

    fun initShaders() {
        Rect.initShader()
        Rect2Corners.initShader()
        RectOutline.initShader()
    }

    /**
     * Draws a rounded rectangle
     */
    fun drawRoundedRectangle(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        if (!Rect.isInitialized() || !Rect.shader.usable) return

        Rect.shader.bind()
        Rect.shaderRadiusUniform.setValue(radius)
        Rect.shaderInnerRectUniform.setValue(x + radius, y + radius, x + width - radius, y + height - radius)

        UIBlock.drawBlockWithActiveShader(matrixStack, color, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        Rect.shader.unbind()
    }

    fun drawRoundedRectangle2Corners(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color, cornerID: Int) {
        if (!Rect2Corners.isInitialized() || !Rect2Corners.shader.usable) return

        Rect2Corners.shader.bind()
        Rect2Corners.shaderRadiusUniform.setValue(radius)
        Rect2Corners.shaderInnerRectUniform.setValue(x + radius, y + radius, x + width - radius, y + height - radius)
        Rect2Corners.shaderCornerIDsUniform.setValue(cornerID)

        UIBlock.drawBlockWithActiveShader(matrixStack, color, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        Rect2Corners.shader.unbind()
    }

    fun drawRoundedRectangleOutline(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color, thickness: Float) {
        if (!RectOutline.isInitialized() || !RectOutline.shader.usable) return

        RectOutline.shader.bind()
        RectOutline.shaderRadiusUniform.setValue(radius)
        RectOutline.shaderInnerRectUniform.setValue(x + radius, y + radius, x + width - radius, y + height - radius)
        RectOutline.shaderOutlineThickness.setValue(thickness)

        UIBlock.drawBlockWithActiveShader(matrixStack, color, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        RectOutline.shader.unbind()
    }

    object Rect {
        lateinit var shader: UShader
        lateinit var shaderRadiusUniform: FloatUniform
        lateinit var shaderInnerRectUniform: Float4Uniform

        fun isInitialized() = ::shader.isInitialized

        fun initShader() {
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
    }

    object Rect2Corners {
        lateinit var shader: UShader
        lateinit var shaderRadiusUniform: FloatUniform
        lateinit var shaderInnerRectUniform: Float4Uniform
        lateinit var shaderCornerIDsUniform: IntUniform

        fun isInitialized() = ::shader.isInitialized

        fun initShader() {
            if (::shader.isInitialized) return

            shader = createLegacyShader("rectangle", "roundedRectangle2Corners", BlendState.NORMAL)
            if (!shader.usable) {
                println("Failed to load Odin rounded rectangle (2 corners) shader")
                return
            }
            shaderRadiusUniform = shader.getFloatUniform("u_Radius")
            shaderInnerRectUniform = shader.getFloat4Uniform("u_InnerRect")
            shaderCornerIDsUniform = shader.getIntUniform("u_CornerID")
            println("Loaded Odin rounded rectangle (2 corners) shader")
        }
    }

    object RectOutline {
        lateinit var shader: UShader
        lateinit var shaderRadiusUniform: FloatUniform
        lateinit var shaderInnerRectUniform: Float4Uniform
        lateinit var shaderOutlineThickness: FloatUniform

        fun isInitialized() = ::shader.isInitialized

        fun initShader() {
            if (::shader.isInitialized) return

            shader = createLegacyShader("rectangle", "roundedRectangleOutline", BlendState.NORMAL)
            if (!shader.usable) {
                println("Failed to load Odin rounded rectangle (outline) shader")
                return
            }
            shaderRadiusUniform = shader.getFloatUniform("u_Radius")
            shaderInnerRectUniform = shader.getFloat4Uniform("u_InnerRect")
            shaderOutlineThickness = shader.getFloatUniform("u_OutlineThickness")
            println("Loaded Odin rounded rectangle (outline) shader")
        }
    }
}
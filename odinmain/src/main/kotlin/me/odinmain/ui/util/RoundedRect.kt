package me.odinmain.ui.util

import gg.essential.elementa.components.UIBlock
import gg.essential.universal.UMatrixStack
import gg.essential.universal.shader.*
import me.odinmain.utils.createLegacyShader
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.modMessage
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object RoundedRect {

    fun initShaders() {
        Rect.initShader()
        Rect2Corners.initShader()
        RectOutline.initShader()
        Testing.initShader()
    }

    /**
     * Draws a rounded rectangle
     */
    fun drawRoundedRectangle(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        if (!Rect.isInitialized() || !Rect.shader.usable) return

        Rect.shader.bind()
        Rect.shaderRadiusUniform.setValue(radius)
        Rect.shaderInnerRectUniform.setValue(x + radius, y + radius, x + width - radius, y + height - radius)

        UIBlock.drawBlockWithActiveShader(matrixStack, color.javaColor, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        Rect.shader.unbind()
    }

    fun drawRoundedRectangle2Corners(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color, cornerID: Int) {
        if (!Rect2Corners.isInitialized() || !Rect2Corners.shader.usable) return

        Rect2Corners.shader.bind()
        Rect2Corners.shaderRadiusUniform.setValue(radius)
        Rect2Corners.shaderInnerRectUniform.setValue(x + radius, y + radius, x + width - radius, y + height - radius)
        Rect2Corners.shaderCornerIDsUniform.setValue(cornerID)

        UIBlock.drawBlockWithActiveShader(matrixStack, color.javaColor, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        Rect2Corners.shader.unbind()
    }

    fun drawRoundedRectangleOutline(matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color, thickness: Float) {
        if (!RectOutline.isInitialized() || !RectOutline.shader.usable) return

        RectOutline.shader.bind()
        RectOutline.shaderRadiusUniform.setValue(radius)
        RectOutline.shaderInnerRectUniform.setValue(x + radius, y + radius, x + width - radius, y + height - radius)
        RectOutline.shaderOutlineThickness.setValue(thickness)

        UIBlock.drawBlockWithActiveShader(matrixStack, color.javaColor, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        RectOutline.shader.unbind()
    }

    fun drawRectangle(
        matrixStack: UMatrixStack, x: Float, y: Float, width: Float, height: Float,
        color: Color, borderColor: Color, shadowColor: Color,
        borderThickness: Float, topL: Float, topR: Float, botL: Float, botR: Float, edgeSoftness: Float,
        color2: Color, gradientDir: Int, shadowSoftness: Float = 0f
    ) {
        if (!Testing.isInitialized() || !Testing.shader.usable) return

        Testing.shader.bind()
        Testing.shaderCenterUniform.setValue(x + (width / 2), y + (height / 2))
        Testing.shaderSizeUniform.setValue(width, height)
        Testing.shaderRadiusUniform.setValue(botR, topR, botL, topL)
        Testing.shaderBorderThicknessUniform.setValue(borderThickness)
        Testing.shaderEdgeSoftnessUniform.setValue(edgeSoftness)
        Testing.shaderColorUniform.setValue(color.r / 255f, color.g / 255f, color.b / 255f, color.alpha)
        Testing.shaderColor2Uniform.setValue(color2.r / 255f, color2.g / 255f, color2.b / 255f, color2.alpha)
        val direction = Testing.directionVecs[gradientDir]
        Testing.shaderGradientDir.setValue(direction.first, direction.second)
        Testing.shaderBorderColorUniform.setValue(borderColor.r / 255f, borderColor.g / 255f, borderColor.b / 255f, borderColor.alpha)
        Testing.shaderShadowColorUniform.setValue(shadowColor.r / 255f, shadowColor.g / 255f, shadowColor.b / 255f, shadowColor.alpha)
        Testing.shaderShadowSoftness.setValue(shadowSoftness)

        UIBlock.drawBlockWithActiveShader(matrixStack, color.javaColor, x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble())

        Testing.shader.unbind()
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

    object Testing {
        lateinit var shader: UShader
        lateinit var shaderCenterUniform: Float2Uniform
        lateinit var shaderSizeUniform: Float2Uniform
        lateinit var shaderRadiusUniform: Float4Uniform
        lateinit var shaderBorderThicknessUniform: FloatUniform
        lateinit var shaderEdgeSoftnessUniform: FloatUniform
        lateinit var shaderColorUniform: Float4Uniform
        lateinit var shaderColor2Uniform: Float4Uniform
        lateinit var shaderGradientDir: Float2Uniform
        lateinit var shaderBorderColorUniform: Float4Uniform
        lateinit var shaderShadowColorUniform: Float4Uniform
        lateinit var shaderShadowSoftness: FloatUniform
        val directionVecs = listOf(Pair(1f, 0f), Pair(0f, 1f), Pair(-1f, 0f), Pair(0f, -1f))

        fun isInitialized() = ::shader.isInitialized

        fun initShader() {
            if (::shader.isInitialized) return

            shader = createLegacyShader("rectangle", "testing", BlendState.NORMAL)
            if (!shader.usable) {
                println("Failed to load Odin rounded rectangle (test) shader")
                return
            }
            shaderCenterUniform = shader.getFloat2Uniform("u_rectCenter")
            shaderSizeUniform = shader.getFloat2Uniform("u_rectSize")
            shaderRadiusUniform = shader.getFloat4Uniform("u_Radii")
            shaderBorderThicknessUniform = shader.getFloatUniform("u_borderThickness")
            shaderEdgeSoftnessUniform = shader.getFloatUniform("u_edgeSoftness")
            shaderColorUniform = shader.getFloat4Uniform("u_colorRect")
            shaderColor2Uniform = shader.getFloat4Uniform("u_colorRect2")
            shaderGradientDir = shader.getFloat2Uniform("u_gradientDirectionVector")
            shaderBorderColorUniform = shader.getFloat4Uniform("u_colorBorder")
            shaderShadowColorUniform = shader.getFloat4Uniform("u_colorShadow")
            shaderShadowSoftness = shader.getFloatUniform("u_shadowSoftness")

            println("Loaded Odin rounded rectangle (test) shader")
        }
    }
}
package com.odtheking.odin.utils.render

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.render.CustomRenderPipelines.PIPELINE_ROUND_RECT
import com.odtheking.odin.utils.render.CustomRenderPipelines.PIPELINE_ROUND_RECT_SHADOW
import com.odtheking.odin.utils.render.CustomRenderPipelines.PIPELINE_ROUND_RECT_TEXTURED
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.resources.Identifier
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fc
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object RoundedRectRenderer {

    private const val AA_PADDING = 2f
    private const val HALF_EXTENT_SCALE = 8f
    private const val RADIUS_SCALE = 4f
    private const val MAX_RADIUS = 255f / RADIUS_SCALE

    val FORMAT: VertexFormat = VertexFormat.builder(0)
        .addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
        .addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
        .addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
        .addAttribute(DefaultVertexFormat.UV1_SEMANTIC_NAME, GpuFormat.RG16_SINT)
        .addAttribute(DefaultVertexFormat.UV2_SEMANTIC_NAME, GpuFormat.RG16_SINT)
        .addAttribute(DefaultVertexFormat.LINE_WIDTH_SEMANTIC_NAME, GpuFormat.R32_FLOAT)
        .build()

    fun submit(
        context: GuiGraphicsExtractor,
        x0: Float, y0: Float, x1: Float, y1: Float,
        topLeftColor: Int, topRightColor: Int, bottomRightColor: Int, bottomLeftColor: Int,
        corners: Corners, outlineWidth: Float,
        texture: Identifier? = null,
        clip: ScreenRectangle? = null
    ) = emit(
        context, x0, y0, x1, y1,
        topLeftColor, topRightColor, bottomRightColor, bottomLeftColor,
        corners, outlineWidth, AA_PADDING,
        if (texture != null) PIPELINE_ROUND_RECT_TEXTURED else PIPELINE_ROUND_RECT,
        textureSetup(texture), clip
    )

    fun submitShadow(
        context: GuiGraphicsExtractor,
        x0: Float, y0: Float, x1: Float, y1: Float,
        color: Int, corners: Corners, blur: Float
    ) = emit(
        context, x0, y0, x1, y1, color, color, color, color,
        corners, blur, AA_PADDING + blur,
        PIPELINE_ROUND_RECT_SHADOW, TextureSetup.noTexture(), null
    )

    private fun emit(
        context: GuiGraphicsExtractor,
        x0: Float, y0: Float, x1: Float, y1: Float,
        topLeftColor: Int, topRightColor: Int, bottomRightColor: Int, bottomLeftColor: Int,
        corners: Corners, edgeWidth: Float, padding: Float,
        pipeline: RenderPipeline, textureSetup: TextureSetup,
        clip: ScreenRectangle?
    ) {
        if ((topLeftColor or topRightColor or bottomRightColor or bottomLeftColor) and ALPHA_MASK == 0) return

        val left = min(x0, x1)
        val top = min(y0, y1)
        val right = max(x0, x1)
        val bottom = max(y0, y1)

        val halfWidth = (right - left) * 0.5f
        val halfHeight = (bottom - top) * 0.5f
        if (halfWidth <= 0f || halfHeight <= 0f) return

        val pose = snapshotPose(context.pose())
        val scissor = context.scissorStack.peek()

        val quadX0 = max(floor(left - padding).toInt(), clip?.left() ?: Int.MIN_VALUE)
        val quadY0 = max(floor(top - padding).toInt(), clip?.top() ?: Int.MIN_VALUE)
        val quadX1 = min(ceil(right + padding).toInt(), clip?.right() ?: Int.MAX_VALUE)
        val quadY1 = min(ceil(bottom + padding).toInt(), clip?.bottom() ?: Int.MAX_VALUE)
        if (quadX0 >= quadX1 || quadY0 >= quadY1) return

        val quad = ScreenRectangle(quadX0, quadY0, quadX1 - quadX0, quadY1 - quadY0).transformOutward(pose)
        val bounds = if (scissor != null) scissor.intersection(quad) else quad

        context.guiRenderState.addGuiElement(
            Element(
                pose,
                quadX0.toFloat(), quadY0.toFloat(), quadX1.toFloat(), quadY1.toFloat(),
                (left + right) * 0.5f, (top + bottom) * 0.5f,
                topLeftColor, topRightColor, bottomRightColor, bottomLeftColor,
                fixedPoint(halfWidth), fixedPoint(halfHeight),
                packRadius(corners.topLeft) or (packRadius(corners.topRight) shl 8),
                packRadius(corners.bottomRight) or (packRadius(corners.bottomLeft) shl 8),
                edgeWidth, pipeline, textureSetup,
                scissor, bounds
            )
        )
    }

    private val NO_TEXTURE: TextureSetup = TextureSetup.noTexture()

    private fun textureSetup(texture: Identifier?): TextureSetup {
        if (texture == null) return NO_TEXTURE

        val view = mc.textureManager.getTexture(texture).textureView
        return TextureSetup.singleTexture(view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR))
    }

    private var cachedPose: Matrix3x2f? = null

    private fun snapshotPose(current: Matrix3x2fc): Matrix3x2f {
        val cached = cachedPose
        if (cached != null && cached.equals(current, 0f)) return cached
        return Matrix3x2f(current).also { cachedPose = it }
    }

    private const val ALPHA_MASK = 0xFF shl 24

    private fun fixedPoint(value: Float): Int =
        (value * HALF_EXTENT_SCALE).roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

    private fun packRadius(radius: Float): Int =
        (radius.coerceIn(0f, MAX_RADIUS) * RADIUS_SCALE).roundToInt().coerceIn(0, 255)

    private class Element(
        private val pose: Matrix3x2fc,
        private val x0: Float, private val y0: Float,
        private val x1: Float, private val y1: Float,
        private val centerX: Float, private val centerY: Float,
        private val topLeftColor: Int, private val topRightColor: Int,
        private val bottomRightColor: Int, private val bottomLeftColor: Int,
        private val packedHalfWidth: Int, private val packedHalfHeight: Int,
        private val packedRadiiTop: Int, private val packedRadiiBottom: Int,
        private val edgeWidth: Float,
        private val pipeline: RenderPipeline,
        private val textureSetup: TextureSetup,
        private val scissorArea: ScreenRectangle?,
        private val bounds: ScreenRectangle?
    ) : GuiElementRenderState {

        override fun pipeline() = pipeline
        override fun textureSetup(): TextureSetup = textureSetup
        override fun scissorArea() = scissorArea
        override fun bounds() = bounds

        override fun buildVertices(consumer: VertexConsumer) {
            vertex(consumer, x0, y0, topLeftColor)
            vertex(consumer, x0, y1, bottomLeftColor)
            vertex(consumer, x1, y1, bottomRightColor)
            vertex(consumer, x1, y0, topRightColor)
        }

        private fun vertex(consumer: VertexConsumer, x: Float, y: Float, color: Int) {
            consumer.addVertexWith2DPose(pose, x, y)
                .setColor(color)
                .setUv(x - centerX, y - centerY)
                .setUv1(packedHalfWidth, packedHalfHeight)
                .setUv2(packedRadiiTop, packedRadiiBottom)
                .setLineWidth(edgeWidth)
        }
    }
}
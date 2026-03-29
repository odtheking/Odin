package com.odtheking.odin.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f

class SimpleGuiRenderState(
    val pipeline: RenderPipeline,
    val textureSetup: TextureSetup,
    val scissorArea: ScreenRectangle?,
    val bounds: ScreenRectangle?,
    private val vertexConsumerFunc: (VertexConsumer) -> Unit
) : GuiElementRenderState {

    constructor(
        pipeline: RenderPipeline,
        textureSetup: TextureSetup,
        context: GuiGraphicsExtractor,
        bounds: ScreenRectangle?,
        vertexConsumerFunc: (VertexConsumer) -> Unit
    ) : this(
        pipeline,
        textureSetup,
        context.scissorStack.peek(),
        bounds,
        vertexConsumerFunc
    )

    override fun buildVertices(vertices: VertexConsumer) {
        vertexConsumerFunc(vertices)
    }

    override fun pipeline(): RenderPipeline = pipeline

    override fun textureSetup(): TextureSetup = textureSetup

    override fun scissorArea(): ScreenRectangle? = scissorArea

    override fun bounds(): ScreenRectangle? = bounds

    companion object {
        fun createBounds(x0: Int, y0: Int, x1: Int, y1: Int, pose: Matrix3x2f, scissorArea: ScreenRectangle?): ScreenRectangle? {
            val screenRect = ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose)
            return if (scissorArea != null) scissorArea.intersection(screenRect) else screenRect
        }
    }
}
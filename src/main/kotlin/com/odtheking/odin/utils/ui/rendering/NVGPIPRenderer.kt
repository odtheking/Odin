package com.odtheking.odin.utils.ui.rendering

import com.mojang.blaze3d.opengl.GlConst
import com.mojang.blaze3d.opengl.GlDevice
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState
import org.joml.Matrix3x2f
import org.lwjgl.opengl.GL33C

class NVGPIPRenderer(vertexConsumers: MultiBufferSource.BufferSource) : PictureInPictureRenderer<NVGPIPRenderer.NVGRenderState>(vertexConsumers) {

    override fun renderToTexture(state: NVGRenderState, poseStack: PoseStack) {
        val colorTex = RenderSystem.outputColorTextureOverride ?: return
        val bufferManager = (RenderSystem.getDevice().backend as? GlDevice)?.directStateAccess() ?: return
        val glDepthTex = (RenderSystem.outputDepthTextureOverride?.texture() as? GlTexture) ?: return

        val (width, height) = colorTex.let { it.getWidth(0) to it.getHeight(0) }
        (colorTex.texture() as? GlTexture)?.getFbo(bufferManager, glDepthTex)?.apply {
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this)
            GlStateManager._viewport(0, 0, width, height)
        }

        GL33C.glBindSampler(0, 0)
        NVGRenderer.beginFrame(width.toFloat(), height.toFloat())
        state.renderContent()
        NVGRenderer.endFrame()

        GlStateManager._disableDepthTest()
        GlStateManager._disableCull()
        GlStateManager._enableBlend()
        GlStateManager._blendFuncSeparate(770, 771, 1, 0)
    }

    override fun getTranslateY(height: Int, windowScaleFactor: Int): Float = height / 2f
    override fun getRenderStateClass(): Class<NVGRenderState> = NVGRenderState::class.java
    override fun getTextureLabel(): String = "nvg_renderer"

    data class NVGRenderState(
        private val x: Int,
        private val y: Int,
        private val width: Int,
        private val height: Int,
        private val poseMatrix: Matrix3x2f,
        private val scissor: ScreenRectangle?,
        private val bounds: ScreenRectangle?,
        val renderContent: () -> Unit
    ) : PictureInPictureRenderState {

        override fun scale(): Float = 1f
        override fun x0(): Int = x
        override fun y0(): Int = y
        override fun x1(): Int = x + width
        override fun y1(): Int = y + height
        override fun scissorArea(): ScreenRectangle? = scissor
        override fun bounds(): ScreenRectangle? = bounds
    }

    companion object {
        /**
         * Draw NVG content as a special GUI element.
         *
         * @param context The GuiGraphics to draw to
         * @param x The x position
         * @param y The y position
         * @param width The width of the rendering area
         * @param height The height of the rendering area
         * @param renderContent A lambda that draws the NVG content
         */
        fun draw(
            context: GuiGraphicsExtractor,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            renderContent: () -> Unit
        ) {
            val scissor = context.scissorStack.peek()
            val pose = Matrix3x2f(context.pose())
            val bounds = createBounds(x, y, x + width, y + height, pose, scissor)

            val state = NVGRenderState(
                x, y, width, height,
                pose, scissor, bounds,
                renderContent
            )
            context.guiRenderState.addPicturesInPictureState(state)
        }

        private fun createBounds(x0: Int, y0: Int, x1: Int, y1: Int, pose: Matrix3x2f, scissorArea: ScreenRectangle?): ScreenRectangle? {
            val screenRect = ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose)
            return if (scissorArea != null) scissorArea.intersection(screenRect) else screenRect
        }
    }
}


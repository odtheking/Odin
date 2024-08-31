package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.tessellator
import me.odinmain.utils.render.RenderUtils.worldRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glUseProgram

abstract class FramebufferShader(fragmentShader: String) : Shader(fragmentShader) {
    protected var color = Color.WHITE
    protected var radius: Float = 2f
    protected var quality: Float = 1f

    private var entityShadows = false

    fun startDraw() {
        framebuffer = setupFrameBuffer(framebuffer)
        framebuffer?.bindFramebuffer(false)
        entityShadows = mc.gameSettings.entityShadows
        mc.gameSettings.entityShadows = false
    }

    fun stopDraw(color: Color, radius: Float, quality: Float) {
        mc.gameSettings.entityShadows = entityShadows
        mc.framebuffer.bindFramebuffer(false)
        this.color = color
        this.radius = radius
        this.quality = quality
    }

    fun draw() {
        startShader()

        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        framebuffer?.framebufferRenderEx1t(mc.displayWidth, mc.displayHeight)
        GlStateManager.disableBlend()

        stopShader()
    }
    // still causes issues (best solution is prob writing a shader to merge our frame buffer into the minecraft one and let minecraft render it themselves
    private fun Framebuffer.framebufferRenderEx1t(width: Int, height: Int) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            // Save the current state
            GlStateManager.pushAttrib() // Save the state of all attributes
            GlStateManager.pushMatrix() // Save the current matrix stack

            // Setup for framebuffer rendering
            GlStateManager.colorMask(true, true, true, false) // Allow only RGB colors
            GlStateManager.disableDepth() // Disable depth test
            GlStateManager.depthMask(false) // Disable depth writes

            // Set up orthographic projection for 2D rendering
            GlStateManager.matrixMode(GL_PROJECTION) // Switch to projection matrix
            GlStateManager.pushMatrix() // Save projection matrix
            GlStateManager.loadIdentity()
            GlStateManager.ortho(0.0, width.toDouble(), height.toDouble(), 0.0, 1000.0, 3000.0)

            // Load the modelview matrix for 2D
            GlStateManager.matrixMode(GL_MODELVIEW)
            GlStateManager.pushMatrix() // Save modelview matrix
            GlStateManager.loadIdentity()
            GlStateManager.translate(0.0f, 0.0f, -2000.0f)

            // Set viewport to match the framebuffer size
            GlStateManager.viewport(0, 0, width, height)
            GlStateManager.enableTexture2D() // Ensure texture rendering is enabled
            GlStateManager.disableLighting() // Disable lighting for 2D rendering
            GlStateManager.disableAlpha() // Disable alpha testing for solid colors

            // Reset color to white
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

            // Bind and draw the framebuffer texture
            this.bindFramebufferTexture()
            val texWidth = this.framebufferWidth.toFloat() / this.framebufferTextureWidth.toFloat()
            val texHeight = this.framebufferHeight.toFloat() / this.framebufferTextureHeight.toFloat()

            worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
            worldRenderer.pos(0.0, height.toDouble(), 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex()
            worldRenderer.pos(width.toDouble(), height.toDouble(), 0.0).tex(texWidth.toDouble(), 0.0)
                .color(255, 255, 255, 255).endVertex()
            worldRenderer.pos(width.toDouble(), 0.0, 0.0).tex(texWidth.toDouble(), texHeight.toDouble())
                .color(255, 255, 255, 255).endVertex()
            worldRenderer.pos(0.0, 0.0, 0.0).tex(0.0, texHeight.toDouble()).color(255, 255, 255, 255).endVertex()
            tessellator.draw()
            this.unbindFramebufferTexture()

            // Restore the states that were changed
            GlStateManager.popMatrix() // Restore modelview matrix
            GlStateManager.matrixMode(GL_PROJECTION)
            GlStateManager.popMatrix() // Restore projection matrix
            GlStateManager.matrixMode(GL_MODELVIEW) // Switch back to modelview

            // Reset states
            GlStateManager.depthMask(true)
            GlStateManager.colorMask(true, true, true, true)

            // ** Add this to reset color to default (white) **
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

            // Restore the previous states
            GlStateManager.popMatrix()
            GlStateManager.popAttrib()
        }
    }

    /**
     * @param frameBuffer
     * @return frameBuffer
     * @author TheSlowly
     */
    private fun setupFrameBuffer(frameBuffer: Framebuffer?): Framebuffer {
        return if (frameBuffer == null || frameBuffer.framebufferWidth != mc.displayWidth || frameBuffer.framebufferHeight != mc.displayHeight) {
            Framebuffer(mc.displayWidth, mc.displayHeight, true).apply {
                this.setFramebufferFilter(GL_NEAREST)
                this.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f)
            }
        } else {
            frameBuffer.framebufferClear()
            frameBuffer
        }
    }

    companion object {
        private var framebuffer: Framebuffer? = null
        var setupCameraTransform: () -> Unit = {}
    }
}
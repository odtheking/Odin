package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glUseProgram


abstract class FramebufferShader(fragmentShader: String) : Shader(fragmentShader) {
    protected var color = Color.WHITE
    protected var radius: Float = 2f
    protected var quality: Float = 1f

    private var entityShadows = false

    fun startDraw() {
        GlStateManager.pushMatrix()
        GlStateManager.pushAttrib()
        GlStateManager.disableFog()

        framebuffer = setupFrameBuffer(framebuffer)
        framebuffer?.bindFramebuffer(true)
        entityShadows = mc.gameSettings.entityShadows
        mc.gameSettings.entityShadows = false
        setupCameraTransform.invoke()
    }

    fun stopDraw(color: Color, radius: Float, quality: Float) {
        mc.gameSettings.entityShadows = entityShadows
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        mc.framebuffer.bindFramebuffer(true)
        this.color = color
        this.radius = radius
        this.quality = quality


        startShader()
        mc.entityRenderer.setupOverlayRendering()
        framebuffer?.let { drawFramebuffer(it) }
        stopShader()

        GlStateManager.enableFog()
        GlStateManager.popMatrix()
        GlStateManager.popAttrib()
    }

    /**
     * @param frameBuffer
     * @return frameBuffer
     * @author TheSlowly
     */
    private fun setupFrameBuffer(frameBuffer: Framebuffer?): Framebuffer {
        return if (frameBuffer == null || frameBuffer.framebufferWidth != mc.displayWidth || frameBuffer.framebufferHeight  != mc.displayHeight) {
            Framebuffer(mc.displayWidth, mc.displayHeight, true)
        } else {
            frameBuffer.framebufferClear()
            frameBuffer
        }
    }

    /**=-./'
     * @author TheSlowly
     */
    fun drawFramebuffer(framebuffer: Framebuffer) {
        val scaledResolution = ScaledResolution(mc)
        glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture)
        glBegin(GL_QUADS)
        glTexCoord2d(0.0, 1.0)
        glVertex2d(0.0, 0.0)
        glTexCoord2d(0.0, 0.0)
        glVertex2d(0.0, scaledResolution.scaledHeight.toDouble())
        glTexCoord2d(1.0, 0.0)
        glVertex2d(scaledResolution.scaledWidth.toDouble(), scaledResolution.scaledHeight.toDouble())
        glTexCoord2d(1.0, 1.0)
        glVertex2d(scaledResolution.scaledWidth.toDouble(), 0.0)
        glEnd()
        glUseProgram(0)
    }

    companion object {
        private var framebuffer: Framebuffer? = null
        var setupCameraTransform: () -> Unit = {}
    }
}
package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glUseProgram


abstract class FramebufferShader(fragmentShader: String) : Shader(fragmentShader) {
    protected var red: Float = 0f
    protected var green: Float = 0f
    protected var blue: Float = 0f
    protected var alpha: Float = 1f
    protected var radius: Float = 2f
    protected var quality: Float = 1f

    private var entityShadows = false

    fun startDraw(partialTicks: Float) {
        GlStateManager.enableAlpha()

        GlStateManager.pushMatrix()
        GlStateManager.pushAttrib()

        framebuffer = setupFrameBuffer(framebuffer)
        framebuffer!!.framebufferClear()
        framebuffer!!.bindFramebuffer(true)
        entityShadows = mc.gameSettings.entityShadows
        mc.gameSettings.entityShadows = false
        setupCameraTransform.invoke()
    }

    fun stopDraw(color: Color, radius: Float, quality: Float) {
        mc.gameSettings.entityShadows = entityShadows
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        mc.framebuffer.bindFramebuffer(true)

        red = color.r / 255f
        green = color.g / 255f
        blue = color.b / 255f
        alpha = color.alpha
        this.radius = radius
        this.quality = quality

        mc.entityRenderer.disableLightmap()
        RenderHelper.disableStandardItemLighting()

        startShader()
        mc.entityRenderer.setupOverlayRendering()
        drawFramebuffer(framebuffer)
        stopShader()

        mc.entityRenderer.disableLightmap()

        GlStateManager.popMatrix()
        GlStateManager.popAttrib()
    }

    /**
     * @param frameBuffer
     * @return frameBuffer
     * @author TheSlowly
     */
    fun setupFrameBuffer(frameBuffer: Framebuffer?): Framebuffer {
        var frameBuffer1 = frameBuffer
        frameBuffer1?.deleteFramebuffer()

        frameBuffer1 = Framebuffer(mc.displayWidth, mc.displayHeight, true)

        return frameBuffer1
    }

    /**
     * @author TheSlowly
     */
    fun drawFramebuffer(framebuffer: Framebuffer?) {
        val scaledResolution = ScaledResolution(mc)
        glBindTexture(GL_TEXTURE_2D, framebuffer!!.framebufferTexture)
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
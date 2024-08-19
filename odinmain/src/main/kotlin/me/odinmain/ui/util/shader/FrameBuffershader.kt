package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL30

abstract class FramebufferShader(fragmentShader: String) : Shader(fragmentShader) {
    protected var color = Color.WHITE
    protected var radius: Float = 2f
    protected var quality: Float = 1f

    private var entityShadows = false

    fun startDraw() {
        GlStateManager.pushMatrix()

        framebuffer = setupFrameBuffer(framebuffer)
        framebuffer?.bindFramebuffer(true)
        entityShadows = mc.gameSettings.entityShadows
        mc.gameSettings.entityShadows = false
    }

    fun stopDraw(color: Color, radius: Float, quality: Float) {
        mc.gameSettings.entityShadows = entityShadows
        mc.framebuffer.bindFramebuffer(true)
        this.color = color
        this.radius = radius
        this.quality = quality

        startShader()

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        framebuffer?.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
        GlStateManager.disableBlend();

        stopShader()

        GlStateManager.popMatrix()
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
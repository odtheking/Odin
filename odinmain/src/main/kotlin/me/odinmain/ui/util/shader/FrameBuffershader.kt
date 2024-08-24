package me.odinmain.ui.util.shader

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.Color
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11.*

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
        framebuffer?.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)
        GlStateManager.disableBlend()

        stopShader()
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
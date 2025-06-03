package me.odinmain.utils.ui.shader

import me.odinmain.OdinMain.mc
import me.odinmain.utils.setupCameraTransform
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14

abstract class FramebufferShader3D(frag: String) : FramebufferShader(frag) {
    open fun prepare3dRendering(partialTicks: Float, forceReset: Boolean = false) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        updateFramebufferSize(forceReset)
        framebuffer.clearFramebufferNoUnbind()
        preserveMatrixStates()
        mc.entityRenderer.setupCameraTransform(partialTicks, 0)
    }

    override fun drawShader(zOffset: Float) {
        restoreMatrixStates()
        setupOverlayRendering()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL14.glBlendEquation(GL14.GL_FUNC_ADD)
        mc.framebuffer.bindFramebuffer(true)
        startShader()
        drawFramebuffer(zOffset)
        stopShader()
        resetOverlayRendering()
        GL11.glPopAttrib()
    }

    private fun preserveMatrixStates() {
        GL11.glMatrixMode(GL11.GL_TEXTURE)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPushMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPushMatrix()
    }

    private fun restoreMatrixStates() {
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_TEXTURE)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
    }

    private fun Framebuffer.clearFramebufferNoUnbind() {
        this.bindFramebuffer(true)
        GlStateManager.clearColor(framebufferColor[0], framebufferColor[1], framebufferColor[2], framebufferColor[3])
        var i = 16384
        if (this.useDepth) {
            GlStateManager.clearDepth(1.0)
            i = i or 256
        }

        GlStateManager.clear(i)
    }

    private fun setupOverlayRendering(width: Double? = null, height: Double? = null) {
        val sr = ScaledResolution(mc)
        val w = width ?: sr.scaledWidth_double
        val h = height ?: sr.scaledHeight_double
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, w, h, 0.0, 1000.0, 3000.0)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glTranslatef(0.0f, 0.0f, -2000.0f)
    }

    private fun resetOverlayRendering() {
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
    }
}
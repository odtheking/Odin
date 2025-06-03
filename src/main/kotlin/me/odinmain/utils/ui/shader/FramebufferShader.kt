package me.odinmain.utils.ui.shader

import me.odinmain.OdinMain.mc
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11

abstract class FramebufferShader(frag: String) : Shader("/shaders/vertex.vert", frag) {
    var framebuffer = Framebuffer(mc.displayWidth, mc.displayHeight, true).apply { setFramebufferColor(0f, 0f, 0f, 0f) }

    protected fun updateFramebufferSize(forceReset: Boolean) {
        if (forceReset || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight)
            framebuffer.createBindFramebuffer(mc.displayWidth, mc.displayHeight)
    }

    abstract fun drawShader(zOffset: Float = 0f)

    protected fun drawFramebuffer(zOffset: Number) {
        val sr = ScaledResolution(mc)
        val sw = sr.scaledWidth_double.toFloat()
        val sh = sr.scaledHeight_double.toFloat()
        GL11.glPushMatrix()
        val prevTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture)
        GL11.glTranslatef(0f, 0f, zOffset.toFloat())
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 1f)
        GL11.glVertex3f(0f, 0f, 0f)
        GL11.glTexCoord2f(0f, 0f)
        GL11.glVertex3f(0f, sh, 0f)
        GL11.glTexCoord2f(1f, 0f)
        GL11.glVertex3f(sw, sh, 0f)
        GL11.glTexCoord2f(1f, 1f)
        GL11.glVertex3f(sw, 0f, 0f)
        GL11.glEnd()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTex)
        GL11.glPopMatrix()
    }
}
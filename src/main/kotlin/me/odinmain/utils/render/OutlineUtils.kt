package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.utils.ui.Colors
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*

object OutlineUtils {
    fun outlineEntity(
        event: RenderEntityModelEvent,
        color: Color = Colors.WHITE,
        lineWidth: Float = 2f,
        depth: Boolean = true,
        shouldCancelHurt: Boolean = true
    ) {
        if (shouldCancelHurt) event.entity.hurtTime = 0
        val fancyGraphics = mc.gameSettings.fancyGraphics
        val gamma = mc.gameSettings.gammaSetting
        mc.gameSettings.fancyGraphics = false
        mc.gameSettings.gammaSetting = 100000f

        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)

        checkSetupFBO()

        if (!depth) {
            glDisable(GL_DEPTH_TEST)
            glDepthMask(false)
        }

        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(0xF)

        glStencilFunc(GL_ALWAYS, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        glColorMask(false, false, false, false)
        render(event)

        glStencilFunc(GL_NOTEQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glColorMask(true, true, true, true)

        if (!depth) {
            glEnable(GL_POLYGON_OFFSET_LINE)
            glPolygonOffset(1.0f, -2000000f)
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)

        glLineWidth(lineWidth)
        glEnable(GL_LINE_SMOOTH)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)

        glColor4d(color.red / 255.0, color.green / 255.0, color.blue / 255.0, color.alpha / 255.0)

        render(event)

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        glDisable(GL_STENCIL_TEST)
        glLineWidth(1f)
        glPopAttrib()
        glPopMatrix()

        mc.gameSettings.fancyGraphics = fancyGraphics
        mc.gameSettings.gammaSetting = gamma
    }

    private fun render(event: RenderEntityModelEvent) {
        event.model.render(
            event.entity,
            event.limbSwing,
            event.limbSwingAmount,
            event.ageInTicks,
            event.headYaw,
            event.headPitch,
            event.scaleFactor
        )
    }

    private fun checkSetupFBO() {
        val fbo = mc.framebuffer ?: return
        if (fbo.depthBuffer <= -1) return
        setupFBO(fbo)
        fbo.depthBuffer = -1
    }

    private fun setupFBO(fbo: Framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)
        val stencilDepthBufferId = EXTFramebufferObject.glGenRenderbuffersEXT()
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferId)
        EXTFramebufferObject.glRenderbufferStorageEXT(
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
            mc.displayWidth,
            mc.displayHeight
        )
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferId
        )
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferId
        )
    }
}
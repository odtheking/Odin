package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.RenderEntityModelEvent
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*

/**
 * Modified from LiquidBounce under GPL-3.0
 * https://github.com/CCBlueX/LiquidBounce/blob/legacy/LICENSE
 */
object OutlineUtils {
    fun outlineEntity(
        event: RenderEntityModelEvent,
        color: Color = Color.WHITE,
        lineWidth: Float = 2f,
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
        glColor4d((color.r / 255f).toDouble(), (color.g / 255f).toDouble(), (color.b / 255f).toDouble(), (color.a / 255f).toDouble())
        renderOne(lineWidth)
        render(event)
        renderTwo()
        render(event)
        renderThree()
        render(event)
        renderFour()
        render(event)
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

    private fun renderOne(lineWidth: Float = 2f) {
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(lineWidth)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(0xF)
        glStencilFunc(GL_NEVER, 1, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    private fun renderTwo() {
        glStencilFunc(GL_NEVER, 0, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    }

    private fun renderThree() {
        glStencilFunc(GL_EQUAL, 1, 0xF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    private fun renderFour() {
        glDepthMask(false)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_POLYGON_OFFSET_LINE)
        glPolygonOffset(1.0f, -2000000f)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)
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
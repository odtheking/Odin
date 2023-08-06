package me.odinclient.utils.render.world

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.RenderEntityModelEvent
import me.odinclient.utils.render.Color
import net.minecraft.client.model.ModelBase
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11


// TODO: TEST WITH NEW COLOR :PRAY: IT WORKS
/**
 * Modified from LiquidBounce under GPL-3.0
 * https://github.com/CCBlueX/LiquidBounce/blob/legacy/LICENSE
 */
object OutlineUtils {
    private fun outlineEntity(
        model: ModelBase,
        livingBase: EntityLivingBase?,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float,
        scaleFactor: Float,
        color: Color,
        thickness: Float,
        shouldCancelHurt: Boolean
    ) {
        val fancyGraphics: Boolean = mc.gameSettings.fancyGraphics
        val gamma: Float = mc.gameSettings.gammaSetting
        mc.gameSettings.fancyGraphics = false
        mc.gameSettings.gammaSetting = Float.MAX_VALUE
        if (shouldCancelHurt) livingBase?.hurtTime = 0
        val entity = livingBase as? Entity
        GlStateManager.resetColor()
        setColor(color)
        renderOne(thickness)
        model.render(
            entity,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            headYaw,
            headPitch,
            scaleFactor
        )
        setColor(color)
        renderTwo()
        model.render(
            entity,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            headYaw,
            headPitch,
            scaleFactor
        )
        setColor(color)
        renderThree()
        model.render(
            entity,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            headYaw,
            headPitch,
            scaleFactor
        )
        setColor(color)
        renderFour(color)
        model.render(
            entity,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            headYaw,
            headPitch,
            scaleFactor
        )
        setColor(color)
        renderFive()
        setColor(Color.WHITE)
        mc.gameSettings.fancyGraphics = fancyGraphics
        mc.gameSettings.gammaSetting = gamma
    }

    fun outlineEntity(event: RenderEntityModelEvent, thickness: Float, color: Color, shouldCancelHurt: Boolean) {
        outlineEntity(
            event.model,
            event.entity,
            event.limbSwing,
            event.limbSwingAmount,
            event.ageInTicks,
            event.headYaw,
            event.headPitch,
            event.scaleFactor,
            color,
            thickness,
            shouldCancelHurt
        )
    }

    private fun renderOne(lineWidth: Float) {
        checkSetupFBO()
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glLineWidth(lineWidth)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_STENCIL_TEST)
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT)
        GL11.glClearStencil(0xF)
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xF)
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE)
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
    }

    private fun renderTwo() {
        GL11.glStencilFunc(GL11.GL_NEVER, 0, 0xF)
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE)
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
    }

    private fun renderThree() {
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xF)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP)
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
    }

    private fun renderFour(color: Color) {
        setColor(color)
        GL11.glDepthMask(false)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE)
        GL11.glPolygonOffset(1.0f, -2000000f)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)
    }

    private fun renderFive() {
        GL11.glPolygonOffset(1.0f, 2000000f)
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_STENCIL_TEST)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glPopAttrib()
    }

    private fun setColor(color: Color) {
        GL11.glColor4d(
            (color.r / 255f).toDouble(),
            (color.g / 255f).toDouble(),
            (color.b / 255f).toDouble(),
            (color.a / 255f).toDouble()
        )
    }

    private fun checkSetupFBO() {
        val fbo = mc.framebuffer
        if (fbo != null) {
            if (fbo.depthBuffer > -1) {
                setupFBO(fbo)
                fbo.depthBuffer = -1
            }
        }
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

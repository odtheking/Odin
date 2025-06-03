package me.odinmain.utils.ui.shader

import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.HighlightRenderer.HighlightEntity
import me.odinmain.utils.render.RenderUtils.renderX
import me.odinmain.utils.render.RenderUtils.renderY
import me.odinmain.utils.render.RenderUtils.renderZ
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

object OutlineShader : FramebufferShader3D("/shaders/outline.frag") {

    private var entityShadows = false
    private var radius = 1
    private var clarity = 1f
    private var innerOpacity = 0f

    override fun prepare3dRendering(partialTicks: Float, forceReset: Boolean) {
        OdinMain.isShaderRunning = true
        entityShadows = mc.gameSettings.entityShadows
        mc.gameSettings.entityShadows = false
        enableOutlineMode()
        super.prepare3dRendering(partialTicks, forceReset)
    }

    override fun drawShader(zOffset: Float) {
        disableOutlineMode()
        mc.gameSettings.entityShadows = entityShadows
        OdinMain.isShaderRunning = false
        super.drawShader(zOffset)
    }

    fun outlineEntities(entities: Collection<HighlightEntity>, radius: Int, innerOpacity: Float, partialTicks: Float) {
        prepare3dRendering(partialTicks)

        for ((entity, color) in entities) {
            val camera = Frustum().also { it.setPosition(mc.thePlayer.renderX, mc.thePlayer.renderY, mc.thePlayer.renderZ) }
            if (!shouldRender(entity, camera, mc.thePlayer.renderX, mc.thePlayer.renderY, mc.thePlayer.renderZ)) continue
            outlineColor(color)
            renderEntityNoLighting(entity, partialTicks)
        }
        
        updateVariables(radius, 1f, innerOpacity)
        drawShader()
    }

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("texelSize")

        setupUniform("radius")
        setupUniform("clarity")
        setupUniform("innerOpacity")
    }

    override fun updateUniforms() {
        GL20.glUniform1i(getUniform("texture"), 0)
        GL20.glUniform2f(getUniform("texelSize"), 1f / mc.displayWidth, 1f / mc.displayHeight)
        GL20.glUniform1i(getUniform("radius"), radius)
        GL20.glUniform1f(getUniform("clarity"), clarity)
        GL20.glUniform1f(getUniform("innerOpacity"), innerOpacity)
    }

    private fun updateVariables(radius: Int, clarity: Float, innerOpacity: Float) {
        this.radius = radius
        this.clarity = clarity
        this.innerOpacity = innerOpacity
    }

    private fun enableOutlineMode() {
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
    }

    private fun disableOutlineMode() {
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
    }

    private val COLOR_BUFFER = BufferUtils.createFloatBuffer(4)

    private fun outlineColor(color: Color) {
        COLOR_BUFFER.put(0, color.red / 255.0f)
        COLOR_BUFFER.put(1, color.green / 255.0f)
        COLOR_BUFFER.put(2, color.blue / 255.0f)
        COLOR_BUFFER.put(3, color.alpha / 255.0f)
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, COLOR_BUFFER)
    }

    private fun renderEntityNoLighting(entity: Entity, partialTicks: Float): Boolean {
        if (entity.ticksExisted == 0) {
            entity.lastTickPosX = entity.posX
            entity.lastTickPosY = entity.posY
            entity.lastTickPosZ = entity.posZ
        }

        val (x, y, z) = accuratePosition(entity, partialTicks)
        val yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks
        val res = mc.renderManager.doRenderEntity(entity, x, y, z, yaw, partialTicks, true)
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
        return res
    }

    private fun accuratePosition(entity: Entity, partialTicks: Float): Vec3 {
        return Vec3(
            (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks) - mc.renderManager.getRenderPosX(),
            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks) - mc.renderManager.getRenderPosY(),
            (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks) - mc.renderManager.getRenderPosZ()
        )
    }

    private fun shouldRender(entity: Entity, camera: Frustum, x: Double, y: Double, z: Double): Boolean {
        return if (entity === mc.renderViewEntity && !(mc.renderViewEntity is EntityLivingBase && (mc.renderViewEntity as EntityLivingBase).isPlayerSleeping || mc.gameSettings.thirdPersonView != 0)) false
        else entity.isInRangeToRender3d(x, y, z) && camera.isBoundingBoxInFrustum(entity.entityBoundingBox)
    }

    private fun copyBuffers(frameToCopy: Framebuffer?, frameToPaste: Framebuffer?, buffersToCopy: Int) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            OpenGlHelper.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, frameToCopy!!.framebufferObject)
            OpenGlHelper.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameToPaste!!.framebufferObject)
            GL30.glBlitFramebuffer(
                0, 0, frameToCopy.framebufferWidth, frameToCopy.framebufferHeight,
                0, 0, frameToPaste.framebufferWidth, frameToPaste.framebufferHeight,
                buffersToCopy, GL11.GL_NEAREST,
            )
        }
    }
}
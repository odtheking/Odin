package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.RenderOverlayNoCaching
import me.odinmain.ui.util.shader.OutlineShader
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.RenderUtils.disableOutlineMode
import me.odinmain.utils.render.RenderUtils.enableOutlineMode
import me.odinmain.utils.render.RenderUtils.outlineColor
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.RenderUtils.renderVec
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HighlightRenderer {
    enum class HighlightType {
        Outline, Glow,Boxes, Box2d, Overlay
    }
    data class HighlightEntity(val entity: Entity, val color: Color, val thickness: Float, val depth: Boolean, val boxStyle: Int = 0)
    const val HIGHLIGHT_MODE_DEFAULT = "Outline"
    val highlightModeList = arrayListOf("Outline", "Glow", "Boxes", "Box 2D", "Overlay")
    const val HIGHLIGHT_MODE_DESCRIPTION = "The type of highlight to use."

    private val entityGetters: MutableList<Pair<() -> HighlightType, () -> Collection<HighlightEntity>>> = mutableListOf()
    val entities = mapOf<HighlightType, MutableList<HighlightEntity>>(
        HighlightType.Outline to mutableListOf(),
        HighlightType.Boxes to mutableListOf(),
        HighlightType.Glow to mutableListOf(),
        HighlightType.Box2d to mutableListOf(),
        HighlightType.Overlay to mutableListOf()
    )

    fun addEntityGetter(type: () -> HighlightType, getter: () -> Collection<HighlightEntity>) {
        this.entityGetters.add(type to getter)
    }

    init {
        Executor(200) {
            entities.forEach { it.value.clear() }
            entityGetters.forEach {
                entities[it.first.invoke()]?.addAll(it.second.invoke())
            }
        }.register()
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        entities[HighlightType.Boxes]?.forEach {
            Renderer.drawStyledBox(it.entity.renderBoundingBox, it.color, it.boxStyle, it.thickness, it.depth)
        }
    }

    @SubscribeEvent
    fun onOverlay(event: RenderOverlayNoCaching) {
        entities[HighlightType.Box2d]?.filter { !it.depth || mc.thePlayer.isEntitySeen(it.entity) }?.forEach {
            Renderer.draw2DEntity(it.entity, it.color, it.thickness)
        }
    }

    private fun EntityPlayerSP.isEntitySeen(entityIn: Entity): Boolean {
        return mc.theWorld?.rayTraceBlocks(
            Vec3(this.posX, this.posY + this.getEyeHeight().toDouble(), this.posZ),
            Vec3(entityIn.posX, entityIn.posY + entityIn.eyeHeight.toDouble(), entityIn.posZ), false, true, false
        ) == null
    }

    @JvmStatic
    fun renderEntityOutline(camera: ICamera, partialTicks: Float) {
        val pass = MinecraftForgeClient.getRenderPass()
        OutlineShader.startDraw()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableFog()
        mc.renderManager.setRenderOutlines(true)
        enableOutlineMode()

        entities[HighlightType.Outline]?.forEach {
            if (!shouldRender(it.entity, pass, mc.renderViewEntity, camera, it.entity.renderVec)) return@forEach
            outlineColor(it.color)
            mc.renderManager.renderEntitySimple(it.entity, partialTicks)
        }

        disableOutlineMode()

        RenderHelper.enableStandardItemLighting()
        mc.renderManager.setRenderOutlines(false)

        GlStateManager.enableLighting()
        OutlineShader.stopDraw(Color.WHITE, 0.5f, 1f)
        GlStateManager.enableFog()
        GlStateManager.enableBlend()
        GlStateManager.enableColorMaterial()
        GlStateManager.enableDepth()
        GlStateManager.enableAlpha()
    }

    private fun shouldRender(entity: Entity, pass: Int, renderViewEntity: Entity, camera: ICamera, vec3: Vec3): Boolean {
        if (!entity.shouldRenderInPass(pass)) return false
        val inRangeToRender = entity.isInRangeToRender3d(vec3.xCoord, vec3.yCoord, vec3.zCoord) && (entity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity.entityBoundingBox) || entity.riddenByEntity == mc.thePlayer)
        return (entity != renderViewEntity || mc.gameSettings.thirdPersonView != 0 || (renderViewEntity is EntityLivingBase && renderViewEntity.isPlayerSleeping)) && inRangeToRender && entity is EntityPlayer
    }
}
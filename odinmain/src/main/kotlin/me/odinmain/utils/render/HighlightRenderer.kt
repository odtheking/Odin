package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.RenderOverlayNoCaching
import me.odinmain.ui.util.shader.GlowShader
import me.odinmain.ui.util.shader.OutlineShader
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.glEnable

object HighlightRenderer {
    enum class HighlightType {
        Outline, Glow,Boxes, Box2d, Overlay
    }
    data class HighlightEntity(val entity: Entity, val color: Color, val thickness: Float, val depth: Boolean, val boxStyle: Int = 0)
    const val HIGHLIGHT_MODE_DEFAULT = "Outline"
    val highlightModeList = arrayListOf("Outline", "Glow", "Boxes", "Box 2D", "Overlay")

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
    fun on2d(event: RenderOverlayNoCaching) {
        entities[HighlightType.Box2d]?.filter { !it.depth || mc.thePlayer.isEntitySeen(it.entity) }?.forEach {
            Renderer.draw2DEntity(it.entity, it.color, it.thickness)
        }
        if (entities[HighlightType.Outline]?.isNotEmpty() == true && entities[HighlightType.Overlay]?.isNotEmpty() == true) return
        GlStateManager.pushMatrix()
        mc.renderManager.setRenderOutlines(true)
        RenderUtils.enableOutlineMode()
        if (entities[HighlightType.Outline]?.isNotEmpty() == true) {
            OutlineShader.startDraw()
            entities[HighlightType.Outline]?.filter { (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) && it.entity.isEntityAlive}?.forEach {
                RenderUtils.outlineColor(it.color)
                mc.renderManager.renderEntityStatic(it.entity, event.partialTicks, true)
            }
            OutlineShader.stopDraw(Color.WHITE, (entities[HighlightType.Outline]?.firstOrNull()?.thickness ?: 1f) / 3f, 1f)
        }
        if (entities[HighlightType.Glow]?.isNotEmpty() == true) {
            GlowShader.startDraw()
            entities[HighlightType.Glow]?.filter { (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) && it.entity.isEntityAlive }?.forEach {
                RenderUtils.outlineColor(it.color)
                mc.renderManager.renderEntityStatic(it.entity, event.partialTicks, true)
            }
            GlowShader.endDraw(Color.WHITE, entities[HighlightType.Glow]?.firstOrNull()?.thickness ?: 1f, 1f)
        }
        mc.entityRenderer.disableLightmap()
        RenderUtils.disableOutlineMode()
        mc.renderManager.setRenderOutlines(false)
        GlStateManager.popMatrix()
        glEnable(GL_BLEND)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)
    }

    private fun EntityPlayerSP.isEntitySeen(entityIn: Entity): Boolean {
        return mc.theWorld.rayTraceBlocks(
            Vec3(this.posX, this.posY + this.getEyeHeight().toDouble(), this.posZ),
            Vec3(entityIn.posX, entityIn.posY + entityIn.eyeHeight.toDouble(), entityIn.posZ), false, true, false
        ) == null
    }
}
package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.events.impl.RenderOverlayNoCaching
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HighlightRenderer {
    enum class HighlightType {
        Outline, Boxes, Box2d, Overlay
    }
    data class HighlightEntity(val entity: Entity, val color: Color, val thickness: Float, val depth: Boolean, val boxStyle: Int = 0)
    const val HIGHLIGHT_MODE_DEFAULT = "Outline"

    val highlightModeList = arrayListOf("Outline", "Boxes", "Box 2D", "Overlay")
    const val HIGHLIGHT_MODE_DESCRIPTION = "The type of highlight to use."

    private val entityGetters: MutableList<Pair<() -> HighlightType, () -> Collection<HighlightEntity>>> = mutableListOf()
    val entities = mapOf<HighlightType, MutableList<HighlightEntity>>(
        HighlightType.Outline to mutableListOf(),
        HighlightType.Boxes to mutableListOf(),
        HighlightType.Box2d to mutableListOf(),
        HighlightType.Overlay to mutableListOf()
    )

    fun addEntityGetter(type: () -> HighlightType, getter: () -> Collection<HighlightEntity>) {
        this.entityGetters.add(type to getter)
    }

    init {
        Executor(200, "HighlightRenderer") {
            entities.values.forEach { it.clear() }
            entityGetters.forEach { (type, getter) ->
                entities[type()]?.addAll(getter())
            }
        }.register()
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        entities[HighlightType.Boxes]?.forEach {
            if (!it.entity.isEntityAlive) return@forEach
            Renderer.drawStyledBox(it.entity.renderBoundingBox, it.color, it.boxStyle, it.thickness, it.depth)
        }
    }

    @SubscribeEvent
    fun onRenderModel(event: RenderEntityModelEvent) {
        entities[HighlightType.Outline]?.find { it.entity.isEntityAlive && it.entity == event.entity && (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) }?.let {
            OutlineUtils.outlineEntity(event, it.color, it.thickness)
        }
    }

    @SubscribeEvent
    fun onOverlay(event: RenderOverlayNoCaching) {
        entities[HighlightType.Box2d]?.filter { !it.depth || mc.thePlayer.isEntitySeen(it.entity) }?.forEach {
            Renderer.draw2DEntity(it.entity, it.color, it.thickness)
        }
        /*
        val entitiesToOutline = entities[HighlightType.Outline]?.filter { (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) && it.entity.isEntityAlive} ?: emptyList()
        val entitiesToGlow = entities[HighlightType.Glow]?.filter { (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) && it.entity.isEntityAlive} ?: emptyList()
        if (entitiesToOutline.isEmpty() && entitiesToGlow.isEmpty()) return
        GlStateManager.pushMatrix()
        mc.renderManager.setRenderOutlines(true)
        RenderUtils.enableOutlineMode()
        if (entitiesToOutline.isNotEmpty()) {
            OutlineShader.startDraw()
            entitiesToOutline.forEach {
                RenderUtils.outlineColor(it.color)
                mc.renderManager.renderEntityStatic(it.entity, event.partialTicks, true)
            }
            OutlineShader.stopDraw(Color.WHITE, ((entities[HighlightType.Outline]?.firstOrNull()?.thickness ?: 1f) / 3f).coerceIn(0.4f, 1f), 1f)
        }
        if (entitiesToGlow.isNotEmpty()) {
            GlowShader.startDraw()
            entitiesToGlow.forEach {
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
         */
    }

    private fun EntityPlayerSP.isEntitySeen(entityIn: Entity): Boolean {
        return mc.theWorld?.rayTraceBlocks(
            Vec3(this.posX, this.posY + this.getEyeHeight(), this.posZ),
            Vec3(entityIn.posX, entityIn.posY + entityIn.eyeHeight.toDouble(), entityIn.posZ), false, true, false
        ) == null
    }
}
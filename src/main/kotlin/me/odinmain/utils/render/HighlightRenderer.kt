package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.RenderOverlayNoCaching
import me.odinmain.events.impl.RenderShaderEvent
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.ui.shader.OutlineShader
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HighlightRenderer {
    enum class HighlightType {
        OutlineOverlay, Outline, Overlay, Box2d, Boxes
    }
    data class HighlightEntity(val entity: Entity, val color: Color, val thickness: Float, val depth: Boolean, val boxStyle: Int = 0)
    const val HIGHLIGHT_MODE_DEFAULT = "Overlay Outline"

    val highlightModeList = arrayListOf("OOverlay", "Outline", "Overlay", "Box 2D", "Boxes")
    const val HIGHLIGHT_MODE_DESCRIPTION = "The type of highlight to use."

    private val entityGetters: MutableList<Pair<() -> HighlightType, () -> Collection<HighlightEntity>>> = mutableListOf()
    val entities = mapOf<HighlightType, MutableList<HighlightEntity>>(
        HighlightType.OutlineOverlay to mutableListOf(),
        HighlightType.Outline to mutableListOf(),
        HighlightType.Overlay to mutableListOf(),
        HighlightType.Box2d to mutableListOf(),
        HighlightType.Boxes to mutableListOf()
    )

    fun addEntityGetter(type: () -> HighlightType, getter: () -> Collection<HighlightEntity>) {
        this.entityGetters.add(type to getter)
    }

    init {
        Executor(150, "HighlightRenderer") {
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

//    @SubscribeEvent
//    fun onRenderModel(event: RenderEntityModelEvent) {
//        entities[HighlightType.Outline]?.find { it.entity.isEntityAlive && it.entity == event.entity && (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) }?.let {
//            OutlineUtils.outlineEntity(event, it.color, it.thickness, false)
//        }
//    }

    @SubscribeEvent
    fun onShaderRender(event: RenderShaderEvent) {
        entities[HighlightType.Outline]?.filter { it.entity.isEntityAlive || (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) }?.let {
            OutlineShader.outlineEntities(it, 2, 0f, event.partialTicks)
        }

        entities[HighlightType.Overlay]?.filter { it.entity.isEntityAlive || (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) }?.let {
            OutlineShader.outlineEntities(it, 0, 0.7f, event.partialTicks)
        }

        entities[HighlightType.OutlineOverlay]?.filter { it.entity.isEntityAlive || (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) }?.let {
            OutlineShader.outlineEntities(it, 2, 0.4f, event.partialTicks)
        }
    }

    @SubscribeEvent
    fun onOverlay(event: RenderOverlayNoCaching) {
        entities[HighlightType.Box2d]?.filter { it.entity.isEntityAlive || (!it.depth || mc.thePlayer.isEntitySeen(it.entity)) }?.forEach {
            Renderer.draw2DEntity(it.entity, it.color, it.thickness)
        }
    }

    private fun EntityPlayerSP.isEntitySeen(entityIn: Entity): Boolean {
        return mc.theWorld?.rayTraceBlocks(
            Vec3(this.posX, this.posY + this.getEyeHeight(), this.posZ),
            Vec3(entityIn.posX, entityIn.posY + entityIn.eyeHeight.toDouble(), entityIn.posZ), false, true, false
        ) == null
    }
}
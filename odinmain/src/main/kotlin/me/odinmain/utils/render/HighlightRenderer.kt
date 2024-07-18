package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import net.minecraft.entity.Entity
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HighlightRenderer {
    enum class HighlightType {
        Outline, Boxes, Box2d, Overlay
    }
    data class HighlightEntity(val entity: Entity, val color: Color, val thickness: Float, val depth: Boolean, val boxStyle: Int = 0)
    const val HIGHLIGHT_MODE_DEFAULT = "Outline"
    val highlightModeList = arrayListOf("Outline", "Boxes", "Box 2D", "Overlay")

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
            Renderer.drawStyledBox(it.entity.entityBoundingBox, it.color, it.boxStyle, it.thickness, it.depth)
        }
        entities[HighlightType.Box2d]?.filter { !it.depth || mc.thePlayer.canEntityBeSeen(it.entity) }?.forEach {
            Renderer.draw2DEntity(it.entity, it.thickness * 6, it.color)
        }
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        val entities = entities[HighlightType.Outline]?.filter { !it.depth || mc.thePlayer.canEntityBeSeen(it.entity) } ?: return
        if (entities.isEmpty()) return
        val entity = entities.find { it.entity == event.entity } ?: return
        OutlineUtils.outlineEntity(event, entity.thickness, entity.color, shouldCancelHurt = true)
    }
}
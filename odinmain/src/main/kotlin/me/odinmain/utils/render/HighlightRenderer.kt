package me.odinmain.utils.render

import com.github.stivais.ui.color.Color
import me.odinmain.OdinMain.mc
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import net.minecraft.entity.Entity
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HighlightRenderer {
    enum class HighlightType {
        Outline, Glow, Boxes, Box2d, Overlay
    }
    data class HighlightEntity(val entity: Entity, val color: Color, val thickness: Float, val depth: Boolean, val glowIntensity: Float = 1f)
    const val highlightModeDefault = "Outline"
    val highlightModeList = arrayListOf("Outline", "Glow", "Boxes", "Box 2D", "Overlay")
    val highlightModeList2 = arrayListOf("Outline", "Boxes", "Box 2D", "Glow")

    private val entityGetters: MutableList<Pair<() -> HighlightType, () -> Collection<HighlightEntity>>> = mutableListOf()
    val entities = mapOf<HighlightType, MutableList<HighlightEntity>>(
        HighlightType.Outline to mutableListOf(),
        HighlightType.Glow to mutableListOf(),
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
            Renderer.drawBox(it.entity.renderBoundingBox, it.color, it.thickness, depth = it.depth, fillAlpha = 0)
        }
        entities[HighlightType.Box2d]?.filter { it.depth && mc.thePlayer.canEntityBeSeen(it.entity) }?.forEach {
            Renderer.draw2DEntity(it.entity, it.thickness * 6, it.color)
        }
    }
}
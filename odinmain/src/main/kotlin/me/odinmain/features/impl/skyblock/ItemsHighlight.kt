package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.HighlightRenderer.highlightModeDefault
import me.odinmain.utils.render.HighlightRenderer.highlightModeList
import me.odinmain.utils.skyblock.*
import net.minecraft.client.renderer.entity.RenderEntity
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ItemsHighlight : Module(
    "Item Highlight",
    description = "Outlines dropped item entities.",
    category = Category.RENDER
) {
    private val renderThrough: Boolean by BooleanSetting("Through Walls", true)
    private val mode: Int by SelectorSetting("Mode", highlightModeDefault, highlightModeList)
    private val thickness: Float by NumberSetting("Line Width", 0.5f, 0.2f, 1f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes")
    private val colorStyle: Boolean by DualSetting("Color Style", "Rarity", "Distance", default = false, description = "Which color style to use")
    private val stopRendering: Boolean by BooleanSetting("Don't Render", false, description = "Cancels the rendering of the item.")

    @SubscribeEvent
    fun onRenderModel(event: RenderEntityModelEvent) { if (stopRendering && event.entity is EntityItem) event.isCanceled = true }

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled || !LocationUtils.inSkyblock) emptyList()
            else mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>().map {
                HighlightRenderer.HighlightEntity(it, getEntityOutlineColor(it), thickness, !renderThrough, 1f)
            }
        }
    }

    private fun getEntityOutlineColor(entity: EntityItem): Color {
        return when {
            !colorStyle -> getRarity(entity.entityItem.lore)?.color ?: Color.WHITE
            entity.getDistanceToEntity(mc.thePlayer) <= 3.5 -> Color.GREEN
            entity.ticksExisted <= 11 -> Color.YELLOW
            else -> Color.RED
        }
    }
}
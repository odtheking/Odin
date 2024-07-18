package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ItemsHighlight : Module(
    name = "Item Highlight",
    description = "Outlines dropped item entities.",
    category = Category.RENDER
) {
    private val style: Int by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val colorStyle: Boolean by DualSetting("Color Style", "Rarity", "Distance", default = false, description = "Which color style to use")

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!LocationUtils.inSkyblock) return
        val entities = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityItem>() ?: return
        if (entities.isEmpty()) return
        entities.forEach { entity ->
            Renderer.drawStyledBox(entity.entityBoundingBox, getEntityOutlineColor(entity), style, lineWidth, depthCheck)
        }
    }

    private fun getEntityOutlineColor(entity: EntityItem): Color {
        return when {
            !colorStyle -> getRarity(entity.entityItem.lore)?.color ?: Color.WHITE
            entity.ticksExisted <= 11 -> Color.YELLOW
            entity.getDistanceToEntity(mc.thePlayer) <= 3.5 -> Color.GREEN
            else -> Color.RED
        }
    }
}
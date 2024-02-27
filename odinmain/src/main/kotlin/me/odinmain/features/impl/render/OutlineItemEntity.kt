package me.odinmain.features.impl.render

import me.odinmain.events.impl.RenderEntityOutlineEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.getRarity
import me.odinmain.utils.skyblock.lore
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object OutlineItemEntity : Module(
    "Outline Items",
    description = "Outlines dropped item entities.",
    category = Category.RENDER
) {

    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent) {
        if (event.type === RenderEntityOutlineEvent.Type.XRAY) {
            event.queueEntitiesToOutline { getEntityOutlineColor(it) }
        }
    }

    private fun getEntityOutlineColor(entity: Entity): Int? {
        val item = entity as? EntityItem ?: return null

        val entityItem = item.entityItem
        val internalName = entityItem.lore

        val rarity = getRarity(internalName)
        return rarity?.color?.rgba
    }
}
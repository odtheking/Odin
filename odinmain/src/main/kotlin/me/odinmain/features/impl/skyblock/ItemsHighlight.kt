package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.render.CustomHighlight.renderThrough
import me.odinmain.features.impl.render.CustomHighlight.thickness
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.skyblock.getRarity
import me.odinmain.utils.skyblock.lore
import net.minecraft.entity.item.EntityItem

object ItemsHighlight : Module(
    "Item Highlight",
    description = "Outlines dropped item entities.",
    category = Category.RENDER
) {
    val mode: Int by SelectorSetting("Mode", HighlightRenderer.highlightModeDefault, HighlightRenderer.highlightModeList)

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled) emptyList()
            else mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>().map {
                HighlightRenderer.HighlightEntity(it, getEntityOutlineColor(it), thickness, !renderThrough, 1f)
            }
        }
    }

    private fun getEntityOutlineColor(entity: EntityItem): Color {
        return getRarity(entity.entityItem.lore)?.color ?: Color.WHITE
    }
}
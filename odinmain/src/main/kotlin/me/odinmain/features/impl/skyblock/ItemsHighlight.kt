package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.getRarity
import me.odinmain.utils.skyblock.lore
import net.minecraft.entity.item.EntityItem

object ItemsHighlight : Module(
    name = "Item Highlight",
    description = "Outlines dropped item entities.",
    category = Category.RENDER
) {
    private val mode: Int by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList)
    private val thickness: Float by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style: Int by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val colorStyle: Boolean by DualSetting("Color Style", "Rarity", "Distance", default = false, description = "Which color style to use.")

    private var currentEntityItems = mutableSetOf<EntityItem>()


    init {
        execute(100) {
            currentEntityItems = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityItem>()?.toMutableSet() ?: mutableSetOf()
        }

        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode]}) {
            if (!enabled) emptyList()
            else currentEntityItems.map { HighlightRenderer.HighlightEntity(it, getEntityOutlineColor(it), thickness, depthCheck, style) }
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
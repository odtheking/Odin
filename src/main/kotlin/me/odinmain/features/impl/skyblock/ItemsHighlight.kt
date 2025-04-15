package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonItemDrops
import me.odinmain.utils.skyblock.getRarity
import me.odinmain.utils.skyblock.lore
import me.odinmain.utils.skyblock.unformattedName
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.entity.item.EntityItem

object ItemsHighlight : Module(
    name = "Item Highlight",
    desc = "Highlights items on the ground."
) {
    private val mode by SelectorSetting("Mode", "Overlay", arrayListOf("Boxes", "Box 2D", "Overlay"), desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val onlySecrets by BooleanSetting("Only Secrets", false, desc = "Only highlights secret drops in dungeons.")
    private val thickness by NumberSetting("Line Width", 1f, .1f, 4f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.")
    private val colorList = arrayListOf("Rarity", "Distance", "Custom")
    private val colorStyle by SelectorSetting("Color Style", "Rarity", colorList, desc = "Which color style to use.")
    private val rarityAlpha by NumberSetting("Rarity Alpha", 1f, 0f, 1f, .1f, desc = "The alpha of the rarity color.").withDependency { colorStyle == 0 }
    private val customColor by ColorSetting("Custom Color", Colors.WHITE.withAlpha(1f), true, desc = "The custom color to use.").withDependency { colorStyle == 2 }

    private var currentEntityItems = mutableSetOf<EntityItem>()

    init {
        execute(100) {
            currentEntityItems = mutableSetOf()
            mc.theWorld?.loadedEntityList?.forEach { entity ->
                if (entity !is EntityItem) return@forEach
                if (!onlySecrets || entity.entityItem?.unformattedName?.containsOneOf(dungeonItemDrops, true) == true) currentEntityItems.add(entity)
            }
        }

        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode + 1]}) {
            if (!enabled) emptyList()
            else currentEntityItems.map { HighlightRenderer.HighlightEntity(it, getEntityOutlineColor(it), thickness, depthCheck, style) }
        }
    }

    private fun getEntityOutlineColor(entity: EntityItem): Color {
        return when (colorStyle){
            0 -> getRarity(entity.entityItem.lore)?.color?.withAlpha(rarityAlpha) ?: Colors.WHITE
            1 -> when {
                entity.ticksExisted <= 11 -> Colors.MINECRAFT_YELLOW
                entity.getDistanceToEntity(mc.thePlayer) <= 3.5 -> Colors.MINECRAFT_GREEN
                else -> Colors.MINECRAFT_RED
            }
            else -> customColor
        }
    }
}
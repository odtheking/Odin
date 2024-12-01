package me.odinmain.features.impl.skyblock

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.utils.withAlpha
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonItemDrops
import me.odinmain.utils.skyblock.getRarity
import me.odinmain.utils.skyblock.lore
import me.odinmain.utils.skyblock.unformattedName
import me.odinmain.utils.ui.Colors
import net.minecraft.entity.item.EntityItem

object ItemsHighlight : Module(
    name = "Item Highlight",
    description = "Outlines dropped item entities."
) {
    private val mode by SelectorSetting("Mode", "Overlay", arrayListOf("Boxes", "Box 2D", "Overlay"), description = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val onlySecrets by BooleanSetting("Only Secrets", default = false, description = "Only highlights secret drops in dungeons.")
    private val thickness by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }
    private val depthCheck by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val colorList = arrayListOf("Rarity", "Distance", "Custom")
    private val colorStyle by SelectorSetting("Color Style", "Rarity", colorList, false, description = "Which color style to use.")
    private val customColor by ColorSetting("Custom Color", Color.WHITE.withAlpha(1f), true, description = "The custom color to use.").withDependency { colorStyle == 2 }

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
            0 -> getRarity(entity.entityItem.lore)?.color ?: Color.WHITE
            1 -> {
                if (entity.ticksExisted <= 11) Colors.MINECRAFT_YELLOW
                else if (entity.getDistanceToEntity(mc.thePlayer) <= 3.5) Color.GREEN
                else Color.RED
            }
            else -> customColor
        }
    }
}
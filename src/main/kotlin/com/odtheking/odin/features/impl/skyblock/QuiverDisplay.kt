package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.SetSlotEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.lore
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.world.item.Items

object QuiverDisplay : Module(
    name = "Quiver Display",
    description = "Displays the number of arrows in your quiver."
) {
    private val showName by BooleanSetting("Show Name", true, desc = "Shows the arrow type's name before the count.")
    private val countColor by ColorSetting("Count Color", Colors.MINECRAFT_GREEN, true, desc = "The color of the arrow count.")

    private val quiverHud by HUD("Quiver HUD", "Displays the number of arrows in your quiver.", false) { example ->
        val text = when {
            example -> buildText(Component.literal("Flint Arrow"), "100")
            arrows != null -> arrows?.let { buildText(it.first, it.second) }
            else -> null
        } ?: return@HUD 0 to 0
        return@HUD textDim(text, 0, 0)
    }

    private val arrowRegex = Regex("^Arrows Remaining: ([\\d,]+)$")
    private var arrows: Pair<Component, String>? = null

    private fun buildText(name: Component, count: String) =
        (if (showName) name.copy().append("§8x") else Component.literal(""))
            .append(Component.literal(count).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(countColor.rgba and 0xFFFFFF))))
            .visualOrderText

    init {
        on<SetSlotEvent> {
            if (slotIndex != (if (DungeonUtils.inClear) 9 else 44) || (itemStack.item != Items.FEATHER && itemStack.item != Items.ARROW)) return@on

            arrows = itemStack.hoverName to (itemStack.lore.find { arrowRegex.matches(it.string) }?.let {
                arrowRegex.find(it.string)?.groupValues?.get(1)
            } ?: return@on)
        }
    }
}
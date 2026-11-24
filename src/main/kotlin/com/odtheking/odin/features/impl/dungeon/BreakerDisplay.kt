package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.SetSlotEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.lore
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object BreakerDisplay : Module(
    name = "Breaker Display",
    description = "Displays the amount of charges left in your Dungeon Breaker."
) {
    private val chargesRegex = Regex("Charges: (\\d+)/(\\d+)⸕")
    private var maxCharges = 0
    private var charges = 0

    private val hud by TextHUD(name, "Shows the amount of charges left in your Dungeon Breaker.", false, example = { "§e17§7/§e20§c⸕" }) {
        if (maxCharges != 0 && DungeonUtils.inDungeons && (renderFull || charges != maxCharges)) "§e$charges§7/§e$maxCharges§c⸕" else null
    }
    private val renderFull by BooleanSetting("Render full", false, desc = "Renders the HUD even when the Dungeon Breaker is full.")

    init {
         on<SetSlotEvent> {
            if (!DungeonUtils.inDungeons || itemStack.itemId != "DUNGEONBREAKER") return@on
            itemStack.lore.firstNotNullOfOrNull { chargesRegex.find(it.string) }?.let { match ->
                charges = match.groupValues[1].toIntOrNull() ?: 0
                maxCharges = match.groupValues[2].toIntOrNull() ?: 0
            }
        }
    }
}

package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.loreString
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object BreakerDisplay : Module(
    name = "Breaker Display",
    description = "Displays the amount of charges left in your Dungeon Breaker."
) {
    private val chargesRegex = Regex("Charges: (\\d+)/(\\d+)⸕")
    private var maxCharges = 0
    private var charges = 0

    private val hud by HUD(name, "Shows the amount of charges left in your Dungeon Breaker.", false) {
        if (!it && (maxCharges == 0 || !DungeonUtils.inDungeons || (!renderFull && charges == maxCharges))) 0 to 0
        else textDim("§e${if (it) 17 else charges}§7/§e${if (it) 20 else maxCharges}§c⸕", 0, 0, Colors.WHITE)
    }
    private val renderFull by BooleanSetting("Render full", false, desc = "Renders the HUD even when the Dungeon Breaker is full.")

    init {
        on<GuiEvent.SlotUpdate> {
            if (!DungeonUtils.inDungeons || packet.item?.itemId != "DUNGEONBREAKER") return@on
            packet.item?.loreString?.firstNotNullOfOrNull { chargesRegex.find(it) }?.let { match ->
                charges = match.groupValues[1].toIntOrNull() ?: 0
                maxCharges = match.groupValues[2].toIntOrNull() ?: 0
            }
        }
    }
}

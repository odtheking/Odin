package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.sendCommand

object ExtraStats : Module(
    name = "Extra Stats",
    description = "Shows additional dungeon stats at the end of the run in chat.",
    category = Category.DUNGEON,
) {
    // make this format all the data given from showextrastats instead of just sending the command
    init {
        onMessage("                             > EXTRA STATS <", false) {
            sendCommand("showextrastats")
        }
    }
}

package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.sendCommand

object ExtraStats : Module(
    name = "Extra Stats",
    description = "Shows additional dungeon stats at the end of the run in chat.",
    category = Category.DUNGEON,
) {
    init {
        onMessage("/^\\s*> EXTRA STATS <\$/", false) {
            sendCommand("showextrastats")
        }
    }
}

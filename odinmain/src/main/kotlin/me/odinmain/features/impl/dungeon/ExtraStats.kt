package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.sendCommand

object ExtraStats : Module(
    name = "Extra Stats",
    description = "Automatically clicks the Extra Stats at the end of a dungeon.",
    category = Category.DUNGEON,
) {
    init {
        onMessage("/^\\s*> EXTRA STATS <\$/", false) {
            sendCommand("showextrastats")
        }
    }
}

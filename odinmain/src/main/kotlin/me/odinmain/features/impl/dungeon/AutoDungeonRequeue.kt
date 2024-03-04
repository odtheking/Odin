package me.odinmain.features.impl.dungeon

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.scope
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.skyblock.ChatCommands
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.sendCommand

object AutoDungeonRequeue : Module(
    name = "Auto Requeue",
    description = "Automatically starts a new dungeon at the end of a dungeon.",
    category = Category.DUNGEON
) {
    private val delay: Int by NumberSetting("Delay", 10, 0, 30, 1)
    private val type: Boolean by DualSetting("Type", "Normal", "Requeue", default = true)

    init {
        onMessage("                             > EXTRA STATS <", false) {
            if (ChatCommands.disableRequeue == true) {
                ChatCommands.disableRequeue = false
                return@onMessage
            }
            scope.launch {
                delay(delay * 1000L)
                if (type) sendCommand("instancerequeue")
                else sendCommand("od ${LocationUtils.currentDungeon?.floor?.name?.lowercase()}", true)
            }
        }

    }
}
package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.skyblock.ChatCommands
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.sendCommand

object AutoDungeonRequeue : Module(
    name = "Auto Requeue",
    description = "Automatically starts a new dungeon at the end of a dungeon.",
    category = Category.DUNGEON
) {
    private val delay: Int by NumberSetting("Delay", 10, 0, 30, 1, description = "The delay in seconds before requeuing.")
    private val type: Boolean by DualSetting("Type", "Normal", "Requeue", default = true, description = "The type of command to execute to fulfill the requeue request.")

    init {
        onMessage("                             > EXTRA STATS <", false) {
            if (ChatCommands.disableRequeue == true) {
                ChatCommands.disableRequeue = false
                return@onMessage
            }
            runIn(delay * 20) {
                sendCommand(if (type) "instancerequeue" else "od ${LocationUtils.currentDungeon?.floor?.name?.lowercase()}", clientSide = !type)
            }
        }
    }
}
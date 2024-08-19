package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.sendCommand

object DungeonRequeue : Module(
    name = "Auto Requeue",
    description = "Automatically starts a new dungeon at the end of a dungeon.",
    category = Category.DUNGEON
) {
    private val delay: Int by NumberSetting("Delay", 10, 0, 30, 1, description = "The delay in seconds before requeuing.")
    private val type: Boolean by DualSetting("Type", "Normal", "Requeue", default = true, description = "The type of command to execute to fulfill the requeue request.")
    private val disablePartyLeave: Boolean by BooleanSetting("Disable Party Leave", false, description = "Disables the requeue on party leave message.")

    var disableRequeue = false
    init {
        onMessage(Regex(" {29}> EXTRA STATS <")) {
            if (disableRequeue) {
                disableRequeue = false
                return@onMessage
            }

            runIn(delay * 20) {
                sendCommand(if (type) "instancerequeue" else "od ${DungeonUtils.floor.name.lowercase()}", clientSide = !type)
            }
        }
        onMessage(Regex("(\\[.+])? ?(.{1,16}) has left the party.")) {
            if (disablePartyLeave) disableRequeue = true
        }
        onWorldLoad { disableRequeue = false }
    }
}
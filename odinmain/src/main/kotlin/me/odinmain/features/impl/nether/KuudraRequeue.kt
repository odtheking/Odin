package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.DungeonRequeue.disableRequeue
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.sendCommand

object KuudraRequeue : Module(
    name = "Kuudra Requeue",
    description = "Automatically starts a new kuudra at the end of a kuudra",
    category = Category.NETHER
) {
    private val delay: Int by NumberSetting("Delay", 10, 0, 30, 1, description = "The delay in seconds before requeuing.")
    private val disablePartyLeave: Boolean by BooleanSetting("Disable Party Leave", false, description = "Disables the requeue on party leave message.")

    init {
        onMessage("KUUDRA DOWN!", true) {
            if (disableRequeue) {
                disableRequeue = false
                return@onMessage
            }
            runIn(delay * 20) {
                sendCommand("od t${LocationUtils.kuudraTier}", true)
            }
        }
        onMessage(Regex("(\\[.+])? ?(.{0,16}) has left the party.")) {
            if (disablePartyLeave) disableRequeue = true
        }
    }
}
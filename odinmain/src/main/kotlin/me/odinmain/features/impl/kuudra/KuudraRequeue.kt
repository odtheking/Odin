package me.odinmain.features.impl.kuudra

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.skyblock.ChatCommands
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.sendCommand

object KuudraRequeue : Module(
    name = "Auto Requeue",
    description = "Automatically starts a new kuudra at the end of a kuudra",
    category = Category.KUUDRA
) {
    private val delay: Int by NumberSetting("Delay", 10, 0, 30, 1, description = "The delay in seconds before requeuing.")

    init {
        onMessage("KUUDRA DOWN!", true) {
            if (ChatCommands.disableRequeue == true) {
                ChatCommands.disableRequeue = false
                return@onMessage
            }
            runIn(delay * 20) {
                sendCommand("od t${LocationUtils.kuudraTier}", true)
            }
        }
    }
}
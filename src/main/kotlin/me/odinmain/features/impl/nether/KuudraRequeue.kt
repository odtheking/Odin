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
    description = "Automatically starts a new kuudra at the end of a kuudra.",
    category = Category.NETHER
) {
    private val delay by NumberSetting("Delay", 10, 0, 30, 1, description = "The delay in seconds before requeuing.", unit = "s")
    private val disablePartyLeave by BooleanSetting("Disable Party Leave", false, description = "Disables the requeue on party leave message.")

    init {
        onMessage(Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!\$")) {
            if (disableRequeue) {
                disableRequeue = false
                return@onMessage
            }

            runIn(delay * 20) {
                if (!disableRequeue) {
                    sendCommand("od t${LocationUtils.kuudraTier}", true)
                }
            }
        }

        onMessage(Regex("(\\[.+])? ?(.{1,16}) has (left|been removed from) the party.")) {
            if (disablePartyLeave) disableRequeue = true
        }
    }
}
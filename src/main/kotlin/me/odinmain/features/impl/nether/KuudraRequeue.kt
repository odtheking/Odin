package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.DungeonRequeue.disableRequeue
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.sendCommand

object KuudraRequeue : Module(
    name = "Kuudra Requeue",
    desc = "Automatically starts a new kuudra at the end of a kuudra."
) {
    private val delay by NumberSetting("Delay", 10, 0, 30, 1, desc = "The delay in seconds before requeuing.", unit = "s")
    private val disablePartyLeave by BooleanSetting("Disable Party Leave", false, desc = "Disables the requeue on party leave message.")

    init {
        onMessage(Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!\$")) {
            if (disableRequeue) {
                disableRequeue = false
                return@onMessage
            }

            runIn(delay * 20) {
                if (!disableRequeue) sendCommand("od t${KuudraUtils.kuudraTier}", true)
            }
        }

        onMessage(Regex("(\\[.+])? ?(.{1,16}) has (left|been removed from) the party.")) {
            if (disablePartyLeave) disableRequeue = true
        }
        onMessage(Regex("The party was transferred to (\\[.+])? ?(.{1,16}) because (\\[.+])? ?(.{1,16}) left")) {
            if (disablePartyLeave) disableRequeue = true
        }
        onMessage(Regex("The party was disbanded because all invites expired and the party was empty.")) {
            if (disablePartyLeave) disableRequeue = true
        }
        onMessage(Regex("Kicked (\\[.+])? ?(.{1,16}) because they were offline.")) {
            if (disablePartyLeave) disableRequeue = true
        }

        onMessage(Regex("You have been kicked from the party by (\\[.+])? ?(.{1,16})")) {
            disableRequeue = true
        }

        onMessage(Regex("You left the party.")) {
            disableRequeue = true
        }

        onMessage(Regex("(\\[.+])? ?(.{1,16}) has disbanded the party.")) {
            disableRequeue = true
        }
    }
}
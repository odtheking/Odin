package me.odinmain.features.impl.dungeon

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.sendCommand

object DungeonRequeue : Module(
    name = "Dungeon Requeue",
    desc = "Automatically starts a new dungeon at the end of a dungeon."
) {
    private val delay by NumberSetting("Delay", 2, 0, 30, 1, desc = "The delay in seconds before requeuing.", unit = "s")
    private val type by BooleanSetting("Type", true, desc = "The type of command to execute to fulfill the requeue request. (true for Normal, false for Requeue)")
    private val disablePartyLeave by BooleanSetting("Disable on leave/kick", true, desc = "Disables the requeue on party leave message.")

    var disableRequeue = false
    init {
        onMessage(Regex(" {29}> EXTRA STATS <")) {
            if (disableRequeue) {
                disableRequeue = false
                return@onMessage
            }

            runIn(delay * 20) {
                if (!disableRequeue)
                    sendCommand(if (type) "instancerequeue" else "od ${DungeonUtils.floor?.name?.lowercase()}", clientSide = !type)
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

        onWorldLoad { disableRequeue = false }
    }
}
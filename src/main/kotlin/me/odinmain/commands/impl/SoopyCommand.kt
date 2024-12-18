package me.odinmain.commands.impl

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.commands.commodore
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.skyblock.modMessage

val commands = listOf(
    "nw", "bank", "auctions", "sblvl", "skills", "skillaverage", "overflowskills", "overflowskillaverage", "bestiary",
    "kuudra", "dojo", "faction", "guildof", "dungeon", "currdungeon", "classaverage", "secrets", "essence", "rtca",
    "pet", "nucleus"
)

val soopyCommand = commodore("soopycmd", "spcmd", "spc") {
    literal("help").runs {
        modMessage("Available commands for /spcmd:\n ${commands.joinToString()}")
    }

    runs {
        modMessage("Usage:\n /spcmd <command> <player>\n /spcmd help")
    }

    runs { command: String, user: String? ->
        if (!commands.contains(command)) return@runs modMessage("Invalid Usage. Usage:\n /spcmd <command> <player>\n /spcmd help")

        modMessage("Running command...")
        scope.launch {
            try {
                modMessage(withTimeout(5000) { fetchURLData("https://soopy.dev/api/soopyv2/botcommand?m=$command&u=${user ?: mc.thePlayer.name}") })
            } catch (_: TimeoutCancellationException) {
                modMessage("Request timed out")
            } catch (e: Exception) {
                modMessage("Failed to fetch data: ${e.message}")
            }
        }
    }.suggests("command", commands)
}

package me.odinmain.commands.impl

import kotlinx.coroutines.*
import me.odinmain.OdinMain.mc
import me.odinmain.commands.commodore
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.skyblock.modMessage

val commands = listOf(
    "nw", "bank", "auctions", "sblvl", "skills", "skillaverage", "overflowskills", "overflowskillaverage", "bestiary",
    "kuudra", "dojo", "faction", "guildof", "dungeon", "currdungeon", "classaverage", "secrets", "essence", "rtca",
    "pet", "nucleus"
)

@OptIn(DelicateCoroutinesApi::class)
val soopyCommand = commodore("soopycmd", "spcmd", "spc") {
    literal("help").runs {
        modMessage("Available commands for /spcmd:\n ${commands.joinToString()}")
    }

    runs {
        modMessage("Usage:\n /spcmd <command> <player>\n /spcmd help")
    }

    runs { command: String, user: String? ->
        if (!commands.contains(command)) return@runs modMessage("Invalid Usage. Usage:\n /spcmd <command> <player>\n /spcmd help")
        val targetUser = user ?: mc.thePlayer.name
        val url = "https://soopy.dev/api/soopyv2/botcommand?m=$command&u=$targetUser"

        modMessage("Running command...")
        GlobalScope.launch {
            try {
                val result = withTimeout(5000) {
                    fetchURLData(url)
                }
                modMessage(result)
            } catch (e: TimeoutCancellationException) {
                modMessage("Request timed out")
            } catch (e: Exception) {
                modMessage("Failed to fetch data: ${e.message}")
            }
        }
    }.suggests("command", commands)
}

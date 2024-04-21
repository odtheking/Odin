package me.odinmain.commands.impl

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        GlobalScope.launch { modMessage(fetchURLData(url)) }
    }.suggests("command", commands)
}


/*object SoopyCommand : Commodore {
    override val command: CommandNode =
        literal("spcmd") {

            runs {
                modMessage("Usage: /spcmd <command> [player] || /spcmd cmds")
            }

            runs { str: GreedyString ->
                val message = str.string.split(" ")
                var url = ""
                if (message[0].lowercase().equalsOneOf("cmds", "commands", "cmd", "command"))
                    return@runs modMessage("""Available commands:
                        | kuudra, auctions, skills, skillaverage, dojo, 
                        | overflowskills, overflowskillaverage, bestiary, 
                        | faction, nucleus, guildof, essence, secrets, bank, 
                        | pet, whatdoing, dungeon, currdungeon, sblvl, classaverage, 
                        | rtca, nw.""".trimMargin())

                when (message.size) {
                    1 -> {
                        val playerName = mc.thePlayer.name
                        val command = message[0]
                        url = "https://soopy.dev/api/soopyv2/botcommand?m=$command&u=$playerName"
                    }
                    2 -> {
                        val targetUser = message[1]
                        val command = message[0]
                        url = "https://soopy.dev/api/soopyv2/botcommand?m=$command&u=$targetUser"
                    }
                }
                modMessage("Running command...")

                scope.launch { modMessage(fetchURLData(url)) }
            }
        }
}*/

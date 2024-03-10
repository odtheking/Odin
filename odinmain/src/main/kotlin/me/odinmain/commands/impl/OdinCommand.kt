package me.odinmain.commands.impl

import me.odinmain.OdinMain.display
import me.odinmain.commands.commodore
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.ServerDisplay.colorizePing
import me.odinmain.features.impl.render.ServerDisplay.colorizeTps
import me.odinmain.features.impl.skyblock.DianaHelper
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.sendChatMessage
import me.odinmain.utils.skyblock.sendCommand
import kotlin.math.round

val mainCommand = commodore("od", "odin", "odinclient") {
    runs {
        display = ClickGUI
    }

    literal("reset") {
        literal("clickgui").runs {
            ClickGUIModule.resetPositions()
            modMessage("Reset click gui positions.")
        }
        literal("hud").runs {
            EditHUDGui.resetHUDs()
            modMessage("Reset HUD positions.")
        }
    }

    literal("rq").runs {
        sendCommand("instancerequeue")
        modMessage("requeing dungeon run")
    }

    literal("help").runs {
        modMessage(
            """
             List of commands:
             §3- /od §7» §8Main command.
             §3- /blacklist §7» §8Used to configure your blacklist.
             §3- /highlight §7» §8Used to configure Highlight list.
             §3- /waypoint §7» §8Configure waypoints.
             §3- /termsim §7» §8Simulates terminals so you can practice them.
             §3- /rq §7» §8Requeues dungeon run.
             §3- /od m? » §8Teleports you to a floor in master mode.
             §3- /od f? » §8Teleports you to a floor in normal mode.
             §3- /od t? » §8Teleports you to a kuudra run.
             §3- /od dianareset §7» §8Resets all active diana waypoints.
             §3- /od sendcoords §7» §8Sends coords in patcher's format.
             §3- /od ping §7» §8Sends your ping in chat.
             §3- /od tps §7» §8Sends the server's tps in chat.
             §3- /spcmd §7» §8Use /spcmd cmds for command list.
             """.trimIndent()
        )
    }

    literal("dianareset").runs {
        modMessage("Resetting all active diana waypoints.")
        DianaHelper.burrowsRender.clear()
    }

    literal("sendcoords").runs {
        sendChatMessage("x: ${posX.toInt()}, y: ${posY.toInt()}, z: ${posZ.toInt()}")
    }

    literal("ping").runs {
        modMessage("${colorizePing(ServerUtils.averagePing.toInt())}ms")
    }

    literal("tps").runs {
        modMessage("${colorizeTps(round(ServerUtils.averageTps))}ms")
    }

    runs { str: String ->
        if (str.length != 2) return@runs modMessage("Invalid command. Use /od help for a list of commands.")
        val type = str[0]
        val number = str[1]
        if (!type.equalsOneOf('f', 'm', 't') || number !in '1'..'7') return@runs modMessage("Invalid command. Use /od help for a list of commands.")
        if (type == 't' && number == '1') modMessage("Kuudra doesnt have an option to use a command to join this instance.")
        if (type == 't') sendCommand("joininstance kuudra_${tiers[number]}")
        else if (type == 'f' || type == 'm') sendCommand("joininstance ${if (type == 'm') "master_" else ""}catacombs_floor_${floors[number]}")
    }
/* someone else can do this
    runs { commandString: GreedyString ->
        if (commandString.string.startsWith("-")) {
            val message = commandString.string.replace("-", "").split(" ")
            if (message.size > 2 || message.isEmpty()) return@runs modMessage("Usage: /od -<command> [player] || /spcmd cmds")

            if (message[0].lowercase().equalsOneOf("cmds", "commands", "cmd", "command"))
                return@runs modMessage("""Available commands:
                        | kuudra, auctions, skills, skillaverage, dojo, 
                        | overflowskills, overflowskillaverage, bestiary, 
                        | faction, nucleus, guildof, essence, secrets, bank, 
                        | pet, whatdoing, dungeon, currdungeon, sblvl, classaverage, 
                        | rtca, nw.""".trimMargin())
            var url = ""
            when (message.size) {
                1 -> {
                    val playerName = OdinMain.mc.thePlayer.name
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
            OdinMain.scope.launch { modMessage(fetchURLData(url)) }
        } else {
            if (commandString.string.length != 2) return@runs modMessage("Invalid command. Use /od help for a list of commands.")
            val instanceType = commandString.string[0]
            val instanceNumber = commandString.string[1]
            if (!instanceType.equalsOneOf('f', 'm', 't') || instanceNumber !in '1'..'7') return@runs modMessage("Invalid command. Use /od help for a list of commands.")
            if (instanceType == 't' && instanceNumber == '1') modMessage("Kuudra doesnt have an option to use a command to join this instance.")
            if (instanceType == 't') sendCommand("joininstance kuudra_${tiers[instanceNumber]}")
            else if (instanceType == 'f' || instanceType == 'm') sendCommand("joininstance ${if (instanceType == 'm') "master_" else ""}catacombs_floor_${floors[instanceNumber]}")
        }
    }*/
}

private val floors = mapOf(
    '1' to "one", '2' to "two", '3' to "three", '4' to "four", '5' to "five", '6' to "six", '7' to "seven"
)

private val tiers = mapOf(
    '1' to "basic", '2' to "hot", '3' to "burning", '4' to "fiery", '5' to "infernal"
)

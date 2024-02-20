package me.odinmain.commands.impl

import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.onLegitVersion
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.ServerDisplay.colorizePing
import me.odinmain.features.impl.render.ServerDisplay.colorizeTps
import me.odinmain.features.impl.skyblock.DianaHelper
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.sendChatMessage
import me.odinmain.utils.skyblock.sendCommand
import kotlin.math.round

object OdinCommand : Commodore {

    private val floors = mapOf(
        '1' to "one", '2' to "two", '3' to "three", '4' to "four", '5' to "five", '6' to "six", '7' to "seven"
    )

    private val tiers = mapOf(
        '1' to "basic", '2' to "hot", '3' to "burning", '4' to "fiery", '5' to "infernal"
    )
    override val command: CommandNode =
        literal("od") {
            runs {
                display = ClickGUI
            }

            runs { str: String ->
                if (str.length != 2 || !str[0].equalsOneOf('f', 'm', 't') || str[1] !in '1'..'7') {
                    return@runs modMessage("Invalid command. Use /od help for a list of commands.")
                }
                if (str[0] == 't') sendCommand("joininstance kuudra_${tiers[str[1]]}")
                else if (str[0] == 'f' || str[0] == 'm') sendCommand("joininstance ${if (str[0] == 'm') "master_" else ""}catacombs_floor_${floors[str[1]]}")
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

            if (!onLegitVersion) {
                literal("set") {
                    runs { yaw: Float, pitch: Float ->
                        mc.thePlayer.rotationYaw = yaw.coerceIn(minimumValue = -180f, maximumValue = 180f)
                        mc.thePlayer.rotationPitch = pitch.coerceIn(minimumValue = -90f, maximumValue = 90f)
                    }
                }
            }

            literal("rq").runs {
                sendCommand("instancerequeue")
                modMessage("requeing dungeon run")
            }

            literal("help").runs {
                if (!onLegitVersion) {
                    modMessage(
                        """
                 List of commands:
                 §3- /od » §8Main command.
                 §3- /autosell » §8Used to configure what items are automatically sold with Auto Sell.
                 §3- /blacklist » §8Used to configure your blacklist.
                 §3- /esp » §8Used to configure ESP list.
                 §3- /waypoint » §8Configure waypoints.
                 §3- /termsim » §8Simulates terminals so you can practice them.
                 §3- /rq » §8Requeues dungeon run.
                 §3- /od set {yaw} {float} » §8Sets your yaw and pitch.
                 §3- /od m? » §8Teleports you to a floor in master mode.
                 §3- /od f? » §8Teleports you to a floor in normal mode.
                 §3- /od t? » §8Teleports you to a kuudra run.
                 §3- /od dianareset §7» §8Resets all active diana waypoints.
                 §3- /od sendcoords §7» §8Sends coords in patcher's format.
                 §3- /od ping §7» §8Sends your ping in chat.
                 §3- /od tps §7» §8Sends the server's tps in chat.
                """.trimIndent()
                    )
                } else
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
                 §3- /od sendcoords §7» §8Sends coords in patcher's format.\
                 §3- /od ping §7» §8Sends your ping in chat.
                 §3- /od tps §7» §8Sends the server's tps in chat.
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


        }
}

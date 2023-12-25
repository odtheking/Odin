package me.odinmain.commands.impl

import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.onLegitVersion
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.skyblock.DianaHelper
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.sendChatMessage
import me.odinmain.utils.skyblock.sendCommand

object OdinCommand : Commodore {

    private val map = mapOf(
        '1' to "one", '2' to "two", '3' to "three", '4' to "four", '5' to "five", '6' to "six", '7' to "seven"
    )

    override val command: CommandNode =
        literal("odin") {
            runs {
                display = ClickGUI
            }
            runs { str: String ->
                if (str.length != 2 || !str[0].equalsOneOf('f', 'm') || str[1] !in '1'..'7') {
                    return@runs modMessage("§cInvalid floor.")
                }
                sendCommand("joininstance ${if (str[0] == 'm') "master_" else ""}catacombs_floor_${map[str[1]]}")
            }

            if (!onLegitVersion) {
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
                // some1 else do this
            }

            literal("dianareset").runs {
                modMessage("Resetting all active diana waypoints.")
                DianaHelper.burrowsRender.clear()
            }

            literal("sendcoords").runs {
                sendChatMessage("x: ${posX.toInt()}, y: ${posY.toInt()}, z: ${posZ.toInt()}")
            }

            literal("devmode").runs {
                DevCommand.devMode = !DevCommand.devMode
                modMessage("${if (DevCommand.devMode) "Enabled" else "Disabled"} Developer mode")
            }
        }
}

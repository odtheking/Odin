package me.odinmain.commands.impl

import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.onLegitVersion
import me.odinmain.commands.invoke
import me.odinmain.features.impl.floor7.p3.termsim.StartGui
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.skyblock.DianaHelper
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.sendChatMessage
import me.odinmain.utils.skyblock.sendCommand

val termSimCommand = "termsim" {
    does {
        StartGui.open(it.firstOrNull()?.toLongOrNull() ?: 0L)
    }
}

val mainCommand = "od" {

    "edithud" does {
        display = EditHUDGui
    }

    "reset" {
        sendError("Incorrect usage. Usage: clickgui, hud") // if args aren't met it will send this message

        "clickgui" does {
            ClickGUIModule.resetPositions()
            modMessage("Reset click gui positions.")

        }

        "hud" does {
            EditHUDGui.resetHUDs()
            modMessage("Reset HUD positions.")
        }
    }

    "set" {
        sendError("Incorrect Usage. Usage: rotation, yaw, pitch")
        "rotation" does {
            if (onLegitVersion) return@does modMessage("§cInvalid command! Use `od help` for a list of commands.")

            if (it.size != 2) modMessage("§cMissing yaw and pitch!")
            else {
                if (setRotation(it[0], it[1])) modMessage("Set yaw and pitch to ${it[0]}, ${it[1]}")
                else modMessage("§cInvalid yaw and or pitch.")
            }
        }

        "yaw" does {
            if (onLegitVersion) return@does modMessage("§cInvalid command! Use `od help` for a list of commands.")

            if (it.size != 1) modMessage("§cMissing yaw!")
            else {
                if (setRotation(yaw = it[0])) modMessage("Set yaw to ${it[0]}")
                else modMessage("§cInvalid yaw.")
            }
        }

        "pitch" does {
            if (onLegitVersion) return@does modMessage("§cInvalid command! Use `od help` for a list of commands.")

            if (it.size != 1) modMessage("§cMissing pitch!")
            else {
                if (setRotation(pitch = it[0])) modMessage("Set pitch to ${it[0]}")
                else modMessage("§cInvalid pitch.")
            }
        }
    }

    "rq" does {
        sendCommand("instancerequeue")
        modMessage("requeing dungeon run")
    }

    "help" does {
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
                 §3- /simulate » §8Simulates chat messages.
                 §3- /set yaw » §8Sets your yaw.
                 §3- /set pitch » §8Sets your pitch.
                 §3- /set rotation » §8Sets your yaw and pitch.
                 §3- /m? » §8Teleports you to a floor in master mode.
                 §3- /f? » §8Teleports you to a floor in normal mode.
                 §3- /dianareset §7» §8Resets all active diana waypoints.
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
                 §3- /simulate §7» §8Simulates chat messages.
                 §3- /m? §7» §8Teleports you to a floor in master mode.
                 §3- /f? §7» §8Teleports you to a floor in normal mode.
                 §3- /dianareset §7» §8Resets all active diana waypoints.
                 """.trimIndent()
            )
    }

    "dianareset" does {
        modMessage("Resetting all active diana waypoints.")
        DianaHelper.burrowsRender.clear()
    }

    does {
        if (it.isEmpty()) display = ClickGUI
        else { // test please
            val arg = it[0]
            if (arg.first() == 'f' || arg.first() == 'm') {
                if (arg.length != 2 || !arg[1].isDigit()) return@does modMessage("§cInvalid floorfr!")
                val type = it.first().first()
                val floor = numberMap[it.first()[1].digitToInt()] ?: return@does modMessage("§cInvalid floor!")
                val prefix = if (type == 'm') "master_" else ""
                sendCommand("joininstance ${prefix}catacombs_floor_$floor ")
            } else {
                modMessage("§cInvalid command! Use `od help` for a list of commands.")
            }
        }
    }

    "sendcoords" does {
        sendChatMessage("x: ${mc.thePlayer.posX.toInt()}, y: ${mc.thePlayer.posY.toInt()}, z: ${mc.thePlayer.posZ.toInt()}")
    }
}

private val numberMap = mapOf(1 to "one", 2 to "two", 3 to "three", 4 to "four", 5 to "five", 6 to "six", 7 to "seven")

/** Function to parse strings and set your yaw and pitch. */
private fun setRotation(yaw: String = "0", pitch: String = "0"): Boolean {
    yaw.toFloatOrNull()?.let { mc.thePlayer.rotationYaw = it } ?: return false
    pitch.toFloatOrNull()?.let { mc.thePlayer.rotationPitch = it } ?: return false
    return true
}
package me.odinmain.commands.impl

import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.commands.invoke
import me.odinmain.features.impl.floor7.p3.termsim.StartGui
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.ChatUtils.modMessage

val termSimCommand = "termsim" {
    does {
        StartGui.open()
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
            if (it.size != 2) modMessage("§cMissing yaw and pitch!")
            else {
                if (setRotation(it[0], it[1])) modMessage("Set yaw and pitch to ${it[0]}, ${it[1]}")
                else modMessage("§cInvalid yaw and or pitch.")
            }
        }

        "yaw" does {
            if (it.size != 1) modMessage("§cMissing yaw!")
            else {
                if (setRotation(yaw = it[0])) modMessage("Set yaw to ${it[0]}")
                else modMessage("§cInvalid yaw.")
            }
        }

        "pitch" does {
            if (it.size != 1) modMessage("§cMissing pitch!")
            else {
                if (setRotation(pitch = it[0])) modMessage("Set pitchto ${it[0]}")
                else modMessage("§cInvalid pitch.")
            }
        }
    }

    "rq" does {
        ChatUtils.sendCommand("instancerequeue")
        modMessage("requeing dungeon run")
    }

    "help" {
        does {
            modMessage("""
            List of commands:
             - /od » §7Main command. Do `/od help this` for more info about this command.
             - /autosell » §7Used to configure what items are automatically sold with Auto Sell.
             - /blacklist » §7 Word this to describe what blacklist even does.
             - /esp » §7Used to configure ESP list.
             - /waypoint » §7Configure waypoints.
             - /termsim » §7Simulates terminals so you can practice them.
            """.trimIndent())
        }

        "this" does {
            modMessage("""
                hi someone else do this and dont make it shit 
            """.trimIndent())
        }
    }


    /*"List of commands:" +
            "\n§7od help §8- §7Shows this message." +
            "\n§7od §8- §7Opens the click gui." +
            "\n§7od hud §8- §7Opens the hud editor." +
            "\n§7od reset (clickgui|hud) §8- §7Resets the click gui or hud positions." +
            "\n§7od set (pitch|yaw) # §8- §7Sets your yaw and pitch to #." +
            "\n§7od rq §8- §7Requeues your dungeon run." +
            "\n§7od f# §8- §7Joins floor #." +
            "\n§7od m# §8- §7Joins master floor #." +
            "\n§7termsim §8- §7Opens the term simulator."
*/

    does {
        if (it.isEmpty()) display = ClickGUI
        else { // test please
            val arg = it[0]
            if (arg.first() == 'f' || arg.first() == 'm') {
                if (arg.length != 2 || !arg[1].isDigit()) return@does modMessage("§cInvalid floorfr!")
                val type = it.first().first()
                val floor = numberMap[it.first()[1].digitToInt()] ?: return@does modMessage("§cInvalid floor!")
                val prefix = if (type == 'm') "master_" else ""
                ChatUtils.sendCommand("joininstance ${prefix}catacombs_floor_$floor ")
            } else {
                modMessage("§cInvalid command! Use `od help this` for a list of commands.")
            }
        }
    }
}

private val numberMap = mapOf(1 to "one", 2 to "two", 3 to "three", 4 to "four", 5 to "five", 6 to "six", 7 to "seven")

/** Function to parse strings and set your yaw and pitch. */
private fun setRotation(yaw: String = "0", pitch: String = "0"): Boolean {
    yaw.toFloatOrNull()?.let { mc.thePlayer.rotationYaw = it } ?: return false
    pitch.toFloatOrNull()?.let { mc.thePlayer.rotationPitch = it } ?: return false
    return true
}
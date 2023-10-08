package me.odinclient.commands.impl

import me.odinclient.ModCore.Companion.display
import me.odinclient.ModCore.Companion.mc
import me.odinclient.commands.invoke
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.impl.floor7.p3.termsim.StartGui
import me.odinclient.features.impl.render.ClickGUIModule
import me.odinclient.ui.clickgui.ClickGUI
import me.odinclient.ui.hud.EditHUDGui
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge

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
        }

        "yaw" does {
            if (it.size != 1) return@does modMessage("§cMissing yaw!")
            if (setRotation(yaw = it[0])) modMessage("Set yaw to ${it[0]}")
            else modMessage("§cInvalid yaw.")
        }

        "pitch" does {
            if (it.size != 1) return@does modMessage("§cMissing pitch!")
            if (setRotation(pitch = it[0])) modMessage("Set pitch to ${it[0]}")
            else modMessage("§cInvalid pitch.")
        }

    "rq" {
        ChatUtils.sendCommand("instancerequeue")
        modMessage("requeing dungeon run")
    }

    "simulate" does {
        if (it.isEmpty()) return@does modMessage("§cMissing message!")
        mc.thePlayer.addChatMessage(ChatComponentText(it.joinToString(" ")))
        MinecraftForge.EVENT_BUS.post(ChatPacketEvent(it.joinToString(" ")))
    }

    does {
        if (it.isEmpty()) display = ClickGUI
        else { // test please
            val arg = it[0]
            if (arg == "help")  modMessage(
                "List of commands:" +
                        "\n§7- od help §8- §7Shows this message." +
                        "\n§7- od §8- §7Opens the click gui." +
                        "\n§7- od hud §8- §7Opens the hud editor." +
                        "\n§7- od reset (clickgui|hud) §8- §7Resets the click gui or hud positions." +
                        "\n§7- od set (pitch|yaw) # §8- §7Sets your yaw and pitch to #." +
                        "\n§7- od rq §8- §7Requeues your dungeon run." +
                        "\n§7- od f# §8- §7Joins floor #." +
                        "\n§7- od m# §8- §7Joins master floor #." +
                        "\n§7- termsim §8- §7Opens the term simulator." +
                        "\n§7- od esp help §8- §7Configures the esp commands." +
                        "\n§7- od blacklist help §8- §7Configures the blacklist commands." +
                        "\n§7- od autosell help §8- §7Configures the autosell commands."
            )
            else if (arg.first() == 'f' || arg.first() == 'm') {
                if (arg.length != 2 || !arg[1].isDigit()) return@does modMessage("§cInvalid floor!")
                val type = it.first().first()
                val floor = numberMap[it.first()[1].digitToInt()] ?: return@does modMessage("§cInvalid floor!")
                ChatUtils.sendCommand("joininstance ${if (type == 'm') "master_" else ""}catacombs_floor_$floor ")
            } else {
                modMessage("§cInvalid command! Use od help for a list of commands.")
            }
        }
    }
}

private val numberMap = mapOf(1 to "one", 2 to "two", 3 to "three", 4 to "four", 5 to "five", 6 to "six", 7 to "seven")

/** Function to parse strings and set your yaw and pitch. */
fun setRotation(yaw: String = "0", pitch: String = "0"): Boolean {
    yaw.toFloatOrNull()?.let { mc.thePlayer.rotationYaw = it } ?: return false
    pitch.toFloatOrNull()?.let { mc.thePlayer.rotationPitch = it } ?: return false
    return true


}
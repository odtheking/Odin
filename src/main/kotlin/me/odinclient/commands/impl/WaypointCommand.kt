package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.display
import me.odinclient.commands.Command
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.general.WaypointManager
import me.odinclient.ui.waypoint.WaypointGUI
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ChatUtils.partyMessage
import java.awt.Color
import java.util.*
import kotlin.math.floor

// TODO : REWORK COMMAND TO IMPLEMENT PERMANENT WAYPOINTS
object WaypointCommand : Command("waypoint", listOf("wp", "odwp")) {
    override fun executeCommand(args: Array<String>) {
        if (args.isEmpty()) return modMessage("§cArguments empty. §rUsage: gui, share, here, add, help")
        when (args[0]) {
            "help" -> modMessage("Usage: gui, share, here, add, help")
            "gui" -> display = WaypointGUI

            "share" -> {
                val message = when (args.size) {
                    1 -> "x: ${floor(mc.thePlayer.posX).toInt()}, y: ${floor(mc.thePlayer.posY).toInt()}, z: ${floor(mc.thePlayer.posZ).toInt()}"
                    4 -> "x: ${args[1]}, y: ${args[2]}, z: ${args[3]}"
                    else -> return modMessage("Invalid arguments.")
                }
                partyMessage(message)
            }

            "here" -> {
                WaypointManager.addTempWaypoint(
                    "§fWaypoint",
                    floor(mc.thePlayer.posX).toInt(),
                    floor(mc.thePlayer.posY).toInt(),
                    floor(mc.thePlayer.posZ).toInt()
                )
                modMessage("Added Waypoint at ${floor(mc.thePlayer.posX).toInt()}, ${floor(mc.thePlayer.posY).toInt()}, ${floor(mc.thePlayer.posZ).toInt()}")
            }

            "addtemp" -> {
                if (args.size >= 4) {
                    val values = args.getInt(1, 4) ?: return modMessage("Invalid arguments.")
                    val name = if (args.size == 4) "Waypoint" else args[4]

                    WaypointManager.addTempWaypoint("§f$name", values[0], values[1], values[2])
                    modMessage("Added $name at ${values.joinToString()}.")
                } else modMessage("Invalid arguments.")
            }

            "addperm" -> {
                if (args.size >= 4) {
                    val values = args.getInt(1, 4) ?: return modMessage("Invalid arguments.")
                    val name = if (args.size == 4) "Waypoint" else args[4]
                    val randomColor = Random().run { Color(nextInt(255), nextInt(255), nextInt(255)) }

                    WaypointManager.addWaypoint("§f$name", values[0], values[1], values[2], randomColor)
                    modMessage("Added $name at ${values.joinToString()}.")
                } else modMessage("Invalid arguments.")
            }
            // TODO: show temp waypoints in gui with a timer
            "test" -> {
                WaypointManager.addWaypoint(
                    "§fWaypoint",
                    floor(mc.thePlayer.posX).toInt(),
                    floor(mc.thePlayer.posY).toInt(),
                    floor(mc.thePlayer.posZ).toInt(),
                    Color.RED
                )
            }

            "test2" -> {
                if (args.size == 2) WaypointManager.removeWaypoint(args[1])
            }

            else -> {
                modMessage("§cInvalid usage. §rUsage: gui, share, here, add, help")
            }
        }
    }

    private fun Array<out String>.getInt(start: Int = 0, end: Int = this.size): Array<Int>? {
        val result = mutableListOf<Int>()
        for (i in start until end) {
            try {
                result.add(this[i].toInt())
            } catch (e: NumberFormatException) {
                return null
            }
        }
        return result.toTypedArray()
    }
}
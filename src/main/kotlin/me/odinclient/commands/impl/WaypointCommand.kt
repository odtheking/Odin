package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.display
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.commands.Command
import me.odinclient.commands.CommandArguments
import me.odinclient.features.impl.general.WaypointManager
import me.odinclient.ui.waypoint.WaypointGUI
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ChatUtils.partyMessage
import java.awt.Color
import java.util.*
import kotlin.math.floor

object WaypointCommand : Command("waypoint", listOf("wp", "odwp"), "Command for waypoints. Do /waypoint help for more info.") {

     val randomColor: Color
        get() {
            val random = Random()
            val hue = random.nextFloat()

            val saturation = random.nextFloat() * 0.5f + 0.5f // High saturation
            val brightness = random.nextFloat() * 0.5f + 0.5f // High brightness

            val rgb = Color.HSBtoRGB(hue, saturation, brightness)
            val red = (rgb shr 16) and 0xFF
            val green = (rgb shr 8) and 0xFF
            val blue = rgb and 0xFF

            return Color(red, green, blue)
        }


    override fun executeCommand(args: CommandArguments) {
        if (args.isEmpty()) return modMessage("§cArguments empty. §rUsage: gui, share, here, add, help")
        when (args[0]) {
            "help" -> modMessage(helpMSG)
            "gui" -> display = WaypointGUI

            "share" -> {
                val message = when (args.size) {
                    1 -> "x: ${floor(mc.thePlayer.posX).toInt()} y: ${floor(mc.thePlayer.posY).toInt()} z: ${floor(mc.thePlayer.posZ).toInt()}"
                    4 -> "x: ${args[1]} y: ${args[2]} z: ${args[3]}"
                    else -> return modMessage("§cInvalid arguments, §r/wp share (x y z).")
                }
                partyMessage(message)
            }

            "here" -> {
                if (args.size == 1) return modMessage("§cInvalid arguments, §r/wp here (temp | perm).")
                if (args[1] == "temp")
                    WaypointManager.addTempWaypoint(
                        "§fWaypoint",
                        floor(mc.thePlayer.posX).toInt(),
                        floor(mc.thePlayer.posY).toInt(),
                        floor(mc.thePlayer.posZ).toInt()
                    )
                else if (args[1] == "perm") {
                    WaypointManager.addWaypoint(
                        "§fWaypoint",
                        floor(mc.thePlayer.posX).toInt(),
                        floor(mc.thePlayer.posY).toInt(),
                        floor(mc.thePlayer.posZ).toInt(),
                        randomColor
                    )
                } else {
                    modMessage("§cInvalid arguments, §r/wp here (temp | perm).")
                    return
                }
                modMessage("Added Waypoint at ${floor(mc.thePlayer.posX).toInt()}, ${floor(mc.thePlayer.posY).toInt()}, ${floor(mc.thePlayer.posZ).toInt()}")
            }

            "add" -> {
                if (args.size >= 5) {
                    val values = args.getInt(2, 5) ?: return modMessage("§cInvalid arguments, §r/wp add (temp | perm) x y z.")
                    val name = if (args.size == 5) "Waypoint" else args[5]

                    if (args[1] == "temp")
                        WaypointManager.addTempWaypoint("§f$name", values[0], values[1], values[2])
                    else if (args[1] == "perm")
                        WaypointManager.addWaypoint("§f$name", values[0], values[1], values[2], randomColor)
                    else
                        return modMessage("§cInvalid arguments, §r/wp add (temp | perm) x y z.")

                    modMessage("Added ${if (args[1] == "temp") "temporary" else "permanent"} waypoint: $name at ${values.joinToString()}.")
                } else modMessage("§cInvalid arguments, §r/wp add (temp | perm) x y z.")
            }

            else -> modMessage("§cInvalid usage, usage :\n$helpMSG")
        }
    }

    override val shortcuts: List<String> = listOf("help", "gui", "share", "add")

    private const val helpMSG =
            " - GUI » §7Opens the Gui \n" +
            " - Share (x y z) » §7Sends a message with your current coords, unless coords are specified \n" +
            " - Here (temp | perm) » §7Adds a permanent or temporary waypoint at your current coords\n" +
            " - Add (temp | perm) x y z » §7Adds a permanent or temporary waypoint at the coords specified\n" +
            " - Help » §7Shows this message"

    private fun CommandArguments.getInt(start: Int = 0, end: Int = this.size): List<Int>? {
        val result = mutableListOf<Int>()
        for (i in start until end) {
            try {
                result.add(this[i].toInt())
            } catch (e: NumberFormatException) {
                return null
            }
        }
        return result
    }
}
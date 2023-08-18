package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.display
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.commands.AbstractCommand
import me.odinclient.features.impl.render.WaypointManager
import me.odinclient.ui.waypoint.WaypointGUI
import me.odinclient.utils.Utils.floorToInt
import me.odinclient.utils.VecUtils.floored
import me.odinclient.utils.render.Color
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ChatUtils.partyMessage
import me.odinclient.utils.skyblock.PlayerUtils.posX
import me.odinclient.utils.skyblock.PlayerUtils.posY
import me.odinclient.utils.skyblock.PlayerUtils.posZ
import java.util.*

object WaypointCommand : AbstractCommand(
    "waypoint", "wp", "odwp",
    description = "Command for waypoints. Do /waypoint help for more info."
) {
    init {
        empty { modMessage("§cArguments empty. §rUsage: gui, share, here, add, help") }

        "help" does { modMessage(helpMSG) }
        "gui" does { display = WaypointGUI }

        "share" does {
            val message = when (it.size) {
                0 -> "x: ${posX.floorToInt()} y: ${posY.floorToInt()} z: ${posZ.floorToInt()}"
                3 -> "x: ${it[0]} y: ${it[1]} z: ${it[2]}"
                else -> return@does modMessage("§cInvalid arguments, §r/wp share (x y z).")
            }
            partyMessage(message)
        }

        "here" {
            does {
                modMessage("§cInvalid arguments. §r/wp here (temp | perm).")
            }
            and(
                "temp" does {
                    WaypointManager.addTempWaypoint(vec3 = mc.thePlayer.positionVector.floored())
                    modMessage("Added temporary waypoint.")
                },

                "perm" does {
                    WaypointManager.addWaypoint(vec3 = mc.thePlayer.positionVector.floored(), color = randomColor())
                    modMessage("Added permanent waypoint.")
                }
            )
        }

        "add" {
            does {
                modMessage("§cInvalid arguments. §r/wp add (temp | perm) x y z.")
            }

            and(
                "temp" does {
                    if (it.size != 3) return@does modMessage("§cInvalid coordinates")
                    val pos = it.getInt() ?: return@does modMessage("§cInvalid coordinates")
                    WaypointManager.addTempWaypoint(x = pos[0], y = pos[1], z = pos[2])
                    modMessage("Added temporary waypoint at ${pos[0]}, ${pos[1]}, ${pos[2]}.")

                },
                "perm" does {
                    if (it.size != 3) return@does modMessage("§cInvalid coordinates")
                    val pos = it.getInt() ?: return@does modMessage("§cInvalid coordinates")
                    WaypointManager.addWaypoint(x = pos[0], y = pos[1], z = pos[2], color = randomColor())
                    modMessage("Added permanent waypoint at ${pos[0]}, ${pos[1]}, ${pos[2]}.")
                }
            )
        }

        orElse { modMessage("§cInvalid usage, usage :\n$helpMSG") }
    }

    private const val helpMSG =
        " - GUI » §7Opens the Gui \n" +
                " - Share (x y z) » §7Sends a message with your current coords, unless coords are specified \n" +
                " - Here (temp | perm) » §7Adds a permanent or temporary waypoint at your current coords\n" +
                " - Add (temp | perm) x y z » §7Adds a permanent or temporary waypoint at the coords specified\n" +
                " - Help » §7Shows this message"

    private fun Array<out String>.getInt(start: Int = 0, end: Int = size): List<Int>? {
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

    fun randomColor(): Color {
        val random = Random()

        val hue = random.nextFloat()
        val saturation = random.nextFloat() * 0.5f + 0.5f // High saturation
        val brightness = random.nextFloat() * 0.5f + 0.5f // High brightness

        return Color(hue, saturation, brightness)
    }
}
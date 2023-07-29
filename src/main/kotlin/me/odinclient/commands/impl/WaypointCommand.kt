package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.display
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.commands.AbstractCommand
import me.odinclient.features.impl.general.WaypointManager
import me.odinclient.ui.waypoint.WaypointGUI
import me.odinclient.utils.Utils.floorToInt
import me.odinclient.utils.VecUtils.floored
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ChatUtils.partyMessage
import me.odinclient.utils.skyblock.PlayerUtils.posX
import me.odinclient.utils.skyblock.PlayerUtils.posY
import me.odinclient.utils.skyblock.PlayerUtils.posZ
import net.minecraft.util.Vec3i
import java.awt.Color
import java.util.*

object WaypointCommand : AbstractCommand(
    "waypoint", "wp", "odwp",
    description = "Command for waypoints. Do /waypoint help for more info."
) {
    init {
        empty { modMessage("§cArguments empty. §rUsage: gui, share, here, add, help") }

        "help" does { modMessage(helpMSG) }
        "gui" does { display = WaypointGUI }

        "share"  - {
            does {
                val message = when (it.size) {
                    1 -> "x: ${posX.floorToInt()} y: ${posY.floorToInt()} z: ${posZ.floorToInt()}"
                    4 -> "x: ${it[1]} y: ${it[2]} z: ${it[3]}"
                    else -> return@does modMessage("§cInvalid arguments, §r/wp share (x y z).")
                }
                partyMessage(message)
            }
        }

        "here" - {
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

        "add" - {
            does {
                modMessage("§cInvalid arguments. §r/wp add (temp | perm) x y z.")
            }

            and(
                "temp" - {
                    does {
                        if (it.size < 4) return@does modMessage("§cInvalid coordinates")
                        val pos = it.getVec(2) ?: return@does modMessage("§cInvalid coordinates")
                        WaypointManager.addTempWaypoint(vec3 = pos)
                        modMessage("Added temporary waypoint at ${pos.x}, ${pos.y}, ${pos.z}.")
                    }
                },
                "perm" - {
                    does {
                        if (it.size < 4) return@does modMessage("§cInvalid coordinates")
                        val pos = it.getVec(2) ?: return@does modMessage("§cInvalid coordinates")
                        WaypointManager.addTempWaypoint(vec3 = pos)
                        modMessage("Added permanent waypoint at ${pos.x}, ${pos.y}, ${pos.z}.")
                    }
                    and(
                        "testing" - {}
                    )
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

    private fun Array<out String>.getVec(start: Int = 0): Vec3i? {
        if (this.size < start + 3) return null

        val result = mutableListOf<Int>()
        for (i in start..start + 3) {
            try {
                result.add(this[i].toInt())
            } catch (e: NumberFormatException) {
                return null
            }
        }
        return Vec3i(result[0], result[1], result[2])
    }

    fun randomColor(): Color {
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
}
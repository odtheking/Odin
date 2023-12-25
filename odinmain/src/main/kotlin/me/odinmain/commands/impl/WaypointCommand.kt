package me.odinmain.commands.impl

import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.commands.CommandNode
import me.odinmain.commands.Commodore
import me.odinmain.features.impl.render.WaypointManager
import me.odinmain.features.impl.render.WaypointManager.randomColor
import me.odinmain.ui.waypoint.WaypointGUI
import me.odinmain.utils.floor
import me.odinmain.utils.floored
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage

object WaypointCommand : Commodore {

    override val command: CommandNode =
        literal("waypoint") {

            literal("help").runs {
                modMessage("Waypoint command help:\n") // some1 make good pls thanks
            }

            literal("gui").runs {
                display = WaypointGUI
            }

            literal("share") {
                runs {
                    partyMessage("x: ${posX.floor().toInt()} y: ${posY.floor().toInt()} z: ${posZ.floor().toInt()}")
                }
                runs { x: Double, y: Double, z: Double ->
                    partyMessage("x: $x y: $y, z: $z")
                }
            }

            literal("here") {
                literal("temp").runs {
                    WaypointManager.addTempWaypoint(vec3 = mc.thePlayer.positionVector.floored())
                    modMessage("Added temporary waypoint.")
                }
                literal("perm").runs {
                    WaypointManager.addWaypoint(vec3 = mc.thePlayer.positionVector.floored(), color = randomColor())
                    modMessage("Added permanent waypoint.")
                }
            }

            literal("add") {
                literal("temp").runs { x: Int, y: Int, z: Int -> // honestly should remove temp waypoints
                    WaypointManager.addTempWaypoint(x = x, y = y, z = z)
                    modMessage("Added temporary waypoint at $x, $y, $z.")
                }
                literal("perm").runs { name: String, x: Int, y: Int, z: Int ->
                    WaypointManager.addWaypoint(name, x, y, z)
                    modMessage("Added permanent waypoint at $x, $y, $z.")
                }
            }
        }
}

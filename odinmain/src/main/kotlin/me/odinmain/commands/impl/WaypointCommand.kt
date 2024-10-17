package me.odinmain.commands.impl

import me.odinmain.commands.commodore
import me.odinmain.features.impl.render.WaypointManager
import me.odinmain.utils.floor
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ

val waypointCommand = commodore("waypoint", "odinwaypoint") {

    literal("help").runs {
        modMessage(
            """
                 Waypoint command help:
                 §3- /waypoint » §8Main command.
                 §3- /waypoint share » §8Used to send your location in party chat.
                 §3- /waypoint add temp» §8Used to add temporary waypoints.
                 §3- /waypoint add perm» §8Used to add permanent waypoints.
            """.trimIndent()
        )
    }

    literal("share") {
        runs {
            partyMessage(PlayerUtils.getPositionString())
        }
        runs { x: Double, y: Double, z: Double ->
            partyMessage("x: $x y: $y, z: $z")
        }
    }

    literal("addtemp") {
        runs { x: Int?, y: Int?, z: Int? ->
            val xPos = x ?: posX.floor().toInt(); val yPos = y ?: posY.floor().toInt(); val zPos = z ?: posZ.floor().toInt()
            WaypointManager.addTempWaypoint("Waypoint", xPos, yPos, zPos)
        }

        runs { name: String, x: Int?, y: Int?, z: Int? ->
            val xPos = x ?: posX.floor().toInt(); val yPos = y ?: posY.floor().toInt(); val zPos = z ?: posZ.floor().toInt()
            WaypointManager.addTempWaypoint(name, xPos, yPos, zPos)
        }

        runs {
            val position = PlayerUtils.posFloored
            WaypointManager.addTempWaypoint(name, position.x, position.y, position.z)
        }
    }

    /*literal("gui").runs {  §3- /waypoint gui » §8Used to open the GUI.
       display = WaypointGUI
   }*/

    /*literal("add") {
        // remove temp waypoints its really useless icl
        runs { name: String, x: Int?, y: Int?, z: Int? ->
            val xPos = x ?: posX.floor().toInt(); val yPos = y ?: posY.floor().toInt(); val zPos = z ?: posZ.floor().toInt()
            WaypointManager.addWaypoint(name, xPos, yPos, zPos)
            modMessage("Added permanent waypoint \"$name\" at $xPos, $yPos, $zPos.")
        }
    }*/ // not handling permanent waypoints until we have the gui to manage them
}

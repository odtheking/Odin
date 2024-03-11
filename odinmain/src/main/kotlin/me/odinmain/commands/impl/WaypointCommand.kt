package me.odinmain.commands.impl

import me.odinmain.OdinMain.display
import me.odinmain.commands.commodore
import me.odinmain.features.impl.render.WaypointManager
import me.odinmain.ui.waypoint.WaypointGUI
import me.odinmain.utils.floor
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage

val waypointCommand = commodore("waypoint", "odinwaypoint") {

    literal("help").runs {
        modMessage(
            """
                 Waypoint command help:
                 §3- /waypoint » §8Main command.
                 §3- /waypoint gui » §8Used to open the GUI.
                 §3- /waypoint share » §8Used to send your location in party chat.
                 §3- /waypoint add temp» §8Used to add temporary waypoints.
                 §3- /waypoint add perm» §8Used to add permanent waypoints.
                """.trimIndent()
        )
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

    literal("add") {
        // remove temp waypoints its really useless icl
        runs { name: String, x: Int?, y: Int?, z: Int? ->
            val xPos = x ?: posX.floor().toInt(); val yPos = y ?: posY.floor().toInt(); val zPos = z ?: posZ.floor().toInt()
            WaypointManager.addWaypoint(name, xPos, yPos, zPos)
            modMessage("Added permanent waypoint \"$name\" at $xPos, $yPos, $zPos.")
        }
    }
}

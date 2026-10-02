package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.Waypoints
import com.odtheking.odin.utils.*

val waypointCommand = Commodore("odwaypoint", "odw") {

    literal("share") {
        runs {
            sendChatMessage(getPositionString())
        }
        runs { x: Int, y: Int, z: Int ->
            sendChatMessage("x: $x, y: $y, z: $z")
        }
    }

    literal("addtemp") {
        runs { x: Int, y: Int, z: Int ->
            Waypoints.addTempWaypoint("Waypoint", x, y, z)
        }

        runs { name: String, x: Int?, y: Int?, z: Int? ->
            val (posX, posY, posZ)= mc.player?.blockPosition() ?: return@runs
            Waypoints.addTempWaypoint(name, x ?: posX, y ?: posY, z ?: posZ)
        }

        runs {
            val pos = mc.player?.blockPosition() ?: return@runs
            Waypoints.addTempWaypoint("", pos.x, pos.y, pos.z)
        }
    }
}
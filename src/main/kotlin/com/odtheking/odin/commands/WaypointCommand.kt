package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.TemporaryWaypoints
import com.odtheking.odin.features.impl.render.waypoints.WaypointScreen
import com.odtheking.odin.utils.*

val waypointCommand = Commodore("odwaypoint", "odw", "waypoints") {
    runs {
        mc.schedule { mc.setScreenAndShow(WaypointScreen(null)) }
    }

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
            TemporaryWaypoints.addTempWaypoint("Waypoint", x, y, z)
        }

        runs { name: String, x: Int?, y: Int?, z: Int? ->
            val (posX, posY, posZ)= mc.player?.blockPosition() ?: return@runs
            TemporaryWaypoints.addTempWaypoint(name, x ?: posX, y ?: posY, z ?: posZ)
        }

        runs {
            val pos = mc.player?.blockPosition() ?: return@runs
            TemporaryWaypoints.addTempWaypoint("", pos.x, pos.y, pos.z)
        }
    }
}
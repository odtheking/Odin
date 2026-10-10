package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.SyntaxException
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.render.TemporaryWaypoints
import com.odtheking.odin.features.impl.render.waypoints.WaypointAreas
import com.odtheking.odin.features.impl.render.waypoints.WaypointScreen
import com.odtheking.odin.features.impl.render.waypoints.Waypoints
import com.odtheking.odin.utils.*

val waypointCommand = Commodore("odwaypoint", "odw", "waypoints") {
    runs {
        mc.schedule { mc.setScreenAndShow(WaypointScreen(null)) }
    }

    literal("clear") {
        runs {
            modMessage("§aCleared §f${Waypoints.clear()} §awaypoints.")
        }

        literal("area").executable {
            param("area") {
                parser { area: String ->
                    WaypointAreas.byKey.keys.firstOrNull { it.equals(area, true) } ?: throw SyntaxException("Area not found.")
                }
                suggests { WaypointAreas.all.map { it.key } }
            }

            runs { area: String ->
                modMessage("§aCleared §f${Waypoints.clear(area)} §awaypoints in §f${WaypointAreas.label(area)}§a.")
            }
        }
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
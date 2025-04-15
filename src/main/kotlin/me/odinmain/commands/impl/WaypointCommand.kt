package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.render.WaypointManager
import me.odinmain.utils.component1
import me.odinmain.utils.component2
import me.odinmain.utils.component3
import me.odinmain.utils.floorVec
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import kotlin.math.roundToInt

val waypointCommand = Commodore("waypoint", "odinwaypoint") {

    literal("help").runs {
        modMessage(
            """
                 Waypoint command help:
                 §3- /waypoint » §8Main command.
                 §3- /waypoint share » §8Used to send your location in party chat.
                 §3- /waypoint addtemp » §8Used to add temporary waypoints.
                 §3- /waypoint addtemp <x, y, z> » §8Used to add temporary waypoints.
                 §3- /waypoint addtemp <name, x?, y?, z?> » §8Used to add temporary waypoints.
            """.trimIndent()
        )
    }

    literal("share") {
        runs {
            partyMessage(PlayerUtils.getPositionString())
        }
        runs { x: Int, y: Int, z: Int ->
            partyMessage("x: $x, y: $y, z: $z")
        }
    }

    literal("addtemp") {
        runs { x: Int, y: Int, z: Int ->
            WaypointManager.addTempWaypoint("Waypoint", x, y, z)
        }

        runs { name: String, x: Int?, y: Int?, z: Int? ->
            val (posX, posY, posZ) = mc.thePlayer?.positionVector?.floorVec() ?: return@runs
            WaypointManager.addTempWaypoint(name, x ?: posX.toInt(), y ?: posY.toInt(), z ?: posZ.toInt())
        }

        runs {
            WaypointManager.addTempWaypoint("", posX.roundToInt(), posY.roundToInt(), posZ.roundToInt())
        }
    }
}

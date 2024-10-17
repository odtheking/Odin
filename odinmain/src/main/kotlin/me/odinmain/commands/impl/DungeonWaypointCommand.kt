package me.odinmain.commands.impl

import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.resetSecrets
import me.odinmain.utils.isHexaDecimal
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.BlockPos

val dungeonWaypointsCommand = commodore("dwp", "dungeonwaypoints") {
    runs {
        DungeonWaypoints.onKeybind()
    }

    literal("edit").runs {
        DungeonWaypoints.onKeybind()
    }

    literal("fill").runs {
        DungeonWaypoints.filled = !DungeonWaypoints.filled
        modMessage("Changed fill to: ${DungeonWaypoints.filled}")
    }

    literal("size").runs { size: Double ->
        if (size !in 0.1..1.0) return@runs modMessage("Size is not within 0.1 - 1 !")
        DungeonWaypoints.size = size
        modMessage("Changed size to: ${DungeonWaypoints.size}")
    }

    literal("resetsecrets").runs {
        resetSecrets()
        modMessage("reset secret waypoints")
    }

    literal("type").runs { type: String ->
        DungeonWaypoints.WaypointType.getByName(type)?.let {
            DungeonWaypoints.waypointType = it.ordinal
            modMessage("Changed waypoint type to: ${it.displayName}")
        } ?: modMessage("Invalid type!")
    }

    literal("timer").runs { type: String ->
        DungeonWaypoints.TimerType.getByName(type)?.let {
            DungeonWaypoints.timerSetting = it.ordinal
            modMessage("Changed waypoint type to: ${it.displayName}")
        } ?: modMessage("Invalid type!")
    }

    literal("useblocksize").runs {
        DungeonWaypoints.useBlockSize = !DungeonWaypoints.useBlockSize
        modMessage("Changed use block size to: ${DungeonWaypoints.useBlockSize}")
    }

    literal("offset").runs { x: Double, y: Double, z: Double ->
        DungeonWaypoints.offset = BlockPos(x, y, z)
        modMessage("Next waypoint will be added with an offset of: ${DungeonWaypoints.offset}")
    }

    literal("through").runs {
        DungeonWaypoints.throughWalls = !DungeonWaypoints.throughWalls
        modMessage("Changed through walls to: ${DungeonWaypoints.throughWalls}")
    }

    literal("edittext").runs {
        DungeonWaypoints.editText = !DungeonWaypoints.editText
        modMessage("Changed editing text to: ${DungeonWaypoints.editText}")
    }

    literal("color").runs { hex: String ->
        if (hex.length != 8 || hex.any { !it.isHexaDecimal }) return@runs modMessage("Color hex not properly formatted! Use format RRGGBBAA")
        DungeonWaypoints.color = Color(hex)
        modMessage("Changed color to: $hex")
    }
}

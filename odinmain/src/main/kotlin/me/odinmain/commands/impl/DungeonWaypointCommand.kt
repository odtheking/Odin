package me.odinmain.commands.impl

import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.DungeonWaypoints
import me.odinmain.features.impl.dungeon.DungeonWaypoints.reloadWaypoints
import me.odinmain.utils.isHexaDecimal
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.modMessage

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

    literal("resetwaypoints").runs {
        reloadWaypoints()
    }
    literal("useblocksize").runs {
        DungeonWaypoints.useBlockSize = !DungeonWaypoints.useBlockSize
        modMessage("Changed use block size to: ${DungeonWaypoints.useBlockSize}")
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

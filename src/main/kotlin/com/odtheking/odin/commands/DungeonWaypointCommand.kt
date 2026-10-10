package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.github.stivais.commodore.utils.SyntaxException
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.impl.label
import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.config.WaypointPackFileUtils
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.*
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.setClipboardContent
import kotlinx.coroutines.launch

val dungeonWaypointsCommand = Commodore("dwp", "dungeonwaypoints") {
    runs {
        mc.schedule { mc.setScreenAndShow(WaypointPackSelectorScreen(mc.gui.screen())) }
    }

    literal("fill").runs {
        DungeonWaypoints.filled = !DungeonWaypoints.filled
        modMessage("Fill status changed to: ${DungeonWaypoints.filled}")
    }

    literal("size").runs { sizeX: Double, sizeY: Double, sizeZ: Double ->
        fun valid(size: Double) = size in 0.1..1.0
        if (!valid(sizeX) || !valid(sizeY) || !valid(sizeZ)) return@runs modMessage("§cSize must be between 0.1 and 1.0!")
        DungeonWaypoints.sizeX = sizeX
        DungeonWaypoints.sizeY = sizeY
        DungeonWaypoints.sizeZ = sizeZ
        modMessage("Size changed to: ${sizeX}, ${sizeY}, $sizeZ")
    }

    literal("resetsecrets").runs {
        SecretWaypoints.resetSecrets()
        modMessage("§aSecrets have been reset!")
    }

    literal("type").runs { type: DungeonWaypoints.WaypointType ->
        DungeonWaypoints.waypointType = type
        modMessage("Waypoint type changed to: ${type.label}")
    }

    literal("useblocksize").runs {
        DungeonWaypoints.useBlockSize = !DungeonWaypoints.useBlockSize
        modMessage("Use block size status changed to: ${DungeonWaypoints.useBlockSize}")
    }

    literal("depth").runs {
        DungeonWaypoints.depthCheck = !DungeonWaypoints.depthCheck
        modMessage("Next waypoint will be added with depth check: ${DungeonWaypoints.depthCheck}")
    }

    literal("color").executable {
        param("hex").parser { hex: String ->
            if (!hex.matches(Regex("[0-9A-Fa-f]{8}"))) throw SyntaxException("Color hex not properly formatted! Use format RRGGBBAA")
            Color(hex)
        }

        runs { hex: Color ->
            DungeonWaypoints.color = hex
            modMessage("Color changed to: ${hex.hex()}")
        }
    }

    literal("export").runs {
        scope.launch {
            val encoded = DungeonWaypointConfig.encodeWaypoints(DungeonWaypoints.exportEditableWaypoints())
            if (encoded != null) {
                setClipboardContent(encoded)
                modMessage("Wrote waypoint config to clipboard.")
            } else modMessage("Failed to write waypoint config to clipboard.")
        }
    }

    literal("import").runs { importString: GreedyString? ->
        scope.launch {
            val waypoints = DungeonWaypointConfig.decodeWaypoints(importString?.string) ?: return@launch modMessage("§cFailed to decode waypoints from clipboard. §fIs the data valid?")

            val existing = WaypointPackFileUtils.listPackNames().toSet()
            val name = generateSequence(1) { it + 1 }.map { if (it == 1) "Imported" else "Imported $it" }.first { it !in existing }
            if (!DungeonWaypoints.importPack(name, waypoints)) return@launch modMessage("§cFailed to create pack '$name'.")
            modMessage("Imported waypoints as new pack '$name'!${if (!DungeonWaypoints.enabled) " §7(Make sure to enable the DungeonWayPoints module)" else ""}")
        }
    }
}
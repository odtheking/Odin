package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.config.DungeonWaypointConfig
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.setWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.SecretWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.WaypointPackSelectorScreen
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.setClipboardContent
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.launch

val dungeonWaypointsCommand = Commodore("dwp", "dungeonwaypoints") {
    runs {
        DungeonWaypoints.onKeybind()
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

    literal("type").runs { type: String ->
        DungeonWaypoints.WaypointType.getByName(type)?.let {
            DungeonWaypoints.waypointType = it.ordinal
            modMessage("Waypoint type changed to: ${it.displayName}")
        } ?: modMessage("§cInvalid waypoint type!")
    }

    literal("useblocksize").runs {
        DungeonWaypoints.useBlockSize = !DungeonWaypoints.useBlockSize
        modMessage("Use block size status changed to: ${DungeonWaypoints.useBlockSize}")
    }

    literal("depth").runs {
        DungeonWaypoints.depthCheck = !DungeonWaypoints.depthCheck
        modMessage("Next waypoint will be added with depth check: ${DungeonWaypoints.depthCheck}")
    }

    literal("color").runs { hex: String ->
        if (!hex.matches(Regex("[0-9A-Fa-f]{8}"))) return@runs modMessage("Color hex not properly formatted! Use format RRGGBBAA")
        DungeonWaypoints.color = Color(hex)
        modMessage("Color changed to: $hex")
    }

    literal("export").runs {
        scope.launch {
            val encoded = DungeonWaypointConfig.encodeWaypoints(DungeonWaypoints.editWaypoints)
            if (encoded != null) {
                setClipboardContent(encoded)
                modMessage("Wrote waypoint config to clipboard.")
            } else {
                modMessage("Failed to write waypoint config to clipboard.")
            }
        }
    }

    literal("import").runs {
        scope.launch {
            val clipboard = mc.keyboardHandler?.clipboard?.trim()?.trim { it == '\n' } ?: return@launch modMessage("§cFailed to read a string from clipboard. §fDid you copy it correctly?")
            DungeonWaypoints.editWaypoints = DungeonWaypointConfig.decodeWaypoints(clipboard, clipboard.startsWith("{")) ?: return@launch modMessage("§cFailed to decode waypoints from clipboard. §fIs the data valid?")
            DungeonWaypoints.saveWaypoints()
            DungeonUtils.currentRoom?.setWaypoints()
            modMessage("Imported waypoints from clipboard!${if (!DungeonWaypoints.enabled) "§7(Make sure to enable the DungeonWaypoints module)" else ""}")
        }
    }

    literal("gui").runs {
        schedule(0) { mc.setScreen(WaypointPackSelectorScreen(mc.screen)) }
    }
}
package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.scope
import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.config.DungeonWaypointConfig.decodeWaypoints
import me.odinmain.config.DungeonWaypointConfig.encodeWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.glList
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.setWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.SecretWaypoints.resetSecrets
import me.odinmain.utils.isHexaDecimal
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.writeToClipboard
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.BlockPos

val dungeonWaypointsCommand = Commodore("dwp", "dungeonwaypoints") {
    runs {
        DungeonWaypoints.onKeybind()
    }

    literal("edit").runs {
        DungeonWaypoints.onKeybind()
    }

    literal("fill").runs {
        DungeonWaypoints.filled = !DungeonWaypoints.filled
        modMessage("Fill status changed to: ${DungeonWaypoints.filled}")
    }

    literal("size").runs { size: Double ->
        if (size !in 0.1..1.0) return@runs modMessage("§cSize must be between 0.1 and 1.0!")
        DungeonWaypoints.size = size
        modMessage("Size changed to: ${DungeonWaypoints.size}")
    }

    literal("distance").runs { reach: Int ->
        DungeonWaypoints.distance = reach.toDouble()
    }

    literal("resetsecrets").runs {
        resetSecrets()
        modMessage("§aSecrets have been reset!")
    }

    literal("type").runs { type: String ->
        DungeonWaypoints.WaypointType.getByName(type)?.let {
            DungeonWaypoints.waypointType = it.ordinal
            modMessage("Waypoint type changed to: ${it.displayName}")
        } ?: modMessage("§cInvalid waypoint type!")
    }

    literal("timer").runs { type: String ->
        DungeonWaypoints.TimerType.getByName(type)?.let {
            DungeonWaypoints.timerSetting = it.ordinal
            modMessage("Waypoint timer type changed to: ${it.displayName}")
        } ?: modMessage("§cInvalid timer type!")
    }

    literal("useblocksize").runs {
        DungeonWaypoints.useBlockSize = !DungeonWaypoints.useBlockSize
        modMessage("Use block size status changed to: ${DungeonWaypoints.useBlockSize}")
    }

    literal("offset").runs { x: Double, y: Double, z: Double ->
        DungeonWaypoints.offset = BlockPos(x, y, z)
        modMessage("Next waypoint will be added with an offset of: ${DungeonWaypoints.offset}")
    }

    literal("through").runs {
        DungeonWaypoints.throughWalls = !DungeonWaypoints.throughWalls
        modMessage("Next waypoint will be added with through walls: ${DungeonWaypoints.throughWalls}")
    }

    literal("color").runs { hex: String ->
        if (hex.length != 8 || hex.any { !it.isHexaDecimal }) return@runs modMessage("Color hex not properly formatted! Use format RRGGBBAA")
        DungeonWaypoints.color = Color(hex)
        modMessage("Color changed to: $hex")
    }

    literal("export").runs {
        scope.launch {
            writeToClipboard(encodeWaypoints() ?: return@launch modMessage("Failed to write waypoint config to clipboard."))
            modMessage("Wrote waypoint config to clipboard.")
        }
    }

    literal("import").runs {
        scope.launch {
            val base64Data = GuiScreen.getClipboardString()?.trimEnd { it == '\n' } ?: return@launch modMessage("§cFailed to read a string from clipboard. §fDid you copy it correctly?")
            if (base64Data.startsWith("{")) return@launch modMessage("§eIt looks like you copied json data instead of base64. §f§lEnsure you copied the correct text!")
            val waypoints = decodeWaypoints(base64Data) ?: return@launch // decode waypoints already modmessages this error
            DungeonWaypointConfig.waypoints = waypoints
            DungeonWaypointConfig.saveConfig()

            DungeonUtils.currentRoom?.let {
                setWaypoints(it)
                glList = -1
            }

            modMessage("Imported waypoints from clipboard!")
        }
    }
}
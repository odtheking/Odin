package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.map.tile.DungeonRoom
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.network.WebUtils.gson
import com.odtheking.odin.utils.network.webSocket
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object DungeonMap : Module(
    name = "Dungeon Map",
    description = "Displays the dungeon map."
) {
    private val disableBoss by BooleanSetting("Disable in Boss", true, desc = "Disables the map during boss fights.")

    val backgroundOutline by ColorSetting("Background Outline", Colors.BLACK, true, desc = "The color of the background border.")
    val backgroundColor by ColorSetting("Background Color", Colors.BLACK.withAlpha(0.1f), true, desc = "Background color of the map.")
    val roomText by SelectorSetting("Room Text", "Both", listOf("Both", "Room Name", "Room Secrets"), desc = "What to display on the rooms.")
    val textScaling by NumberSetting("Text Scaling", 0.45f, 0.1f, 1f, 0.05f, desc = "Scale of room name text.")

    private val playerDropdown by DropdownSetting("Player Settings")
    val playerNamesScaling by NumberSetting("Player Names Scaling", 0.75f, 0.1f, 2f, 0.05f, desc = "Scale of player name labels.").withDependency { playerDropdown }
    val playerNameColor by ColorSetting("Player Name Color", Color(70, 70, 70), true, desc = "Color of player name labels.").withDependency { playerDropdown }
    val playerHead by BooleanSetting("Own Player Head", desc = "Shows the player head on the map.").withDependency { playerDropdown }

    private val roomDropdown by DropdownSetting("Room Settings")
    val normalRoomColor by ColorSetting("Normal Room", Color(107, 58, 17), true, desc = "Color of normal rooms.").withDependency { roomDropdown }
    val puzzleRoomColor by ColorSetting("Puzzle Room", Color(117, 0, 133), true, desc = "Color of puzzle rooms.").withDependency { roomDropdown }
    val trapRoomColor by ColorSetting("Trap Room", Color(216, 127, 51), true, desc = "Color of trap rooms.").withDependency { roomDropdown }
    val bloodRoomColor by ColorSetting("Blood Room", Color(255, 0, 0), true, desc = "Color of blood rooms.").withDependency { roomDropdown }
    val entranceRoomColor by ColorSetting("Entrance Room", Color(20, 133, 0), true, desc = "Color of entrance rooms.").withDependency { roomDropdown }
    val fairyRoomColor by ColorSetting("Fairy Room", Color(224, 0, 255), true, desc = "Color of fairy rooms.").withDependency { roomDropdown }
    val championRoomColor by ColorSetting("Champion Room", Color(254, 223, 0), true, desc = "Color of champion rooms.").withDependency { roomDropdown }
    val unknownRoomColor by ColorSetting("Unknown Room", Color(40, 40, 40), true, desc = "Color of unknown rooms hinted by a door with no discovered room on the other side.").withDependency { roomDropdown }

    private val doorDropdown by DropdownSetting("Door Settings")
    val normalDoorColor by ColorSetting("Normal Door", Color(107, 58, 17).darker(), true, desc = "Color of normal doors.").withDependency { doorDropdown }
    val witherDoorColor by ColorSetting("Wither Door", Colors.BLACK, true, desc = "Color of wither doors.").withDependency { doorDropdown }
    val bloodDoorColor by ColorSetting("Blood Door", Color(255, 0, 0), true, desc = "Color of blood room doors.").withDependency { doorDropdown }
    val fairyDoorColor by ColorSetting("Fairy Door", Color(224, 0, 255).darker(), true, desc = "Color of fairy room doors.").withDependency { doorDropdown }
    val unknownDoorColor by ColorSetting("Unknown Door", Color(40, 40, 40).darker(), true, desc = "Color of doors with no discovered room on the other side.").withDependency { doorDropdown }

    val disablePred by BooleanSetting("Disable Prediction", false, desc = "Disables special-column room type prediction.")
    private val allowWebsocket by BooleanSetting("Parse map websocket", true, desc = "Parses the websocket data from your teammates update dungeon secrets in real time.")

    private val exampleRooms by lazy { buildExampleRooms() }
    private val exampleDoors by lazy { buildExampleDoors() }
    private const val MAP_PX = 128

    private val mapHud by HUD("Dungeon Map", "Displays the dungeon map.", false) { example ->
        if ((!DungeonUtils.inDungeons || (disableBoss && DungeonUtils.inBoss)) && !example) return@HUD 0 to 0
        fill(0, 0, MAP_PX, MAP_PX, backgroundColor.rgba)
        hollowFill(0, 0, MAP_PX, MAP_PX, 1, backgroundOutline)
        pose().pushMatrix()

        if (example) {
            pose().translate(5f, 5f)
            renderMap(exampleRooms, exampleDoors, emptyList())
        }
        else {
            pose().translate(DungeonScan.startX.toFloat(), DungeonScan.startY.toFloat())
            pose().scale(DungeonScan.roomSize / 16f)

            renderMap(DungeonScan.rooms, DungeonScan.doors.values, DungeonScan.pathHints)

            if (!DungeonUtils.inBoss) renderPlayers()
        }
        pose().popMatrix()

        MAP_PX to MAP_PX
    }

    val syncSocket = webSocket {
        onMessage { message ->
            if (!allowWebsocket) return@onMessage
            val synced = try { gson.fromJson(message, DungeonRoom::class.java) } catch (_: Exception) { return@onMessage }
            if (synced.data == null) return@onMessage

            val room = DungeonScan.rooms.find { it.topLeft == synced.topLeft } ?: synced.also { new ->
                DungeonScan.rooms.add(new)
                for ((x, z) in new.tiles) {
                    if (x !in 0..5 || z !in 0..5) continue
                    DungeonScan.tiles[x + z * 6].room = new
                }
            }

            if (room.data == null) room.data = synced.data
            if ((room.foundSecrets ?: -1) < (synced.foundSecrets ?: -1)) room.foundSecrets = synced.foundSecrets
            room.walkedInto = true
        }
    }
}
package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.DevModule
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.map.*
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomState
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomType
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.PlayerFaceRenderer
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import java.awt.Color as AwtColor

@DevModule
object DungeonMap : Module(
    name = "Dungeon Map",
    description = "Customizable dungeon map with room colors, door colors, and player names."
) {
    var backgroundColor by ColorSetting("Background Color", Color(0, 0, 0, 0.7f), true, desc = "The background color of the map.")
    var backgroundSize by NumberSetting("Background Size", 5f, 0f, 20f, 1f, desc = "The size of the background border.")

    var textScaling by NumberSetting("Text Scaling", 0.45f, 0.1f, 1f, 0.05f, desc = "Scale of room names.")

    private val playerDropdown by DropdownSetting("Player Settings")
    var playerHeadBackgroundSize by NumberSetting("Player Head BG Size", 1, 0, 10, 1, desc = "Size of player head background.").withDependency { playerDropdown }
    var playerNamesScaling by NumberSetting("Player Names Scaling", 0.75f, 0.1f, 2f, 0.05f, desc = "Scale of player names.").withDependency { playerDropdown }
    var playerNameColor by ColorSetting("Player Name Color", Color(70, 70, 70), false, desc = "Color of player names.").withDependency { playerDropdown }

    private val doorDropdown by DropdownSetting("Door Settings")
    var doorThickness by NumberSetting("Door Thickness", 9, 1, 20, 1, desc = "Thickness of doors on map.").withDependency { doorDropdown }
    var unopenedDoorColor by ColorSetting("Unopened Door", Color(30, 30, 30), false, desc = "Color of unopened doors.").withDependency { doorDropdown }
    var bloodDoorColor by ColorSetting("Blood Door", Colors.MINECRAFT_RED, false, desc = "Color of blood room doors.").withDependency { doorDropdown }
    var witherDoorColor by ColorSetting("Wither Door", Colors.BLACK, false, desc = "Color of wither doors.").withDependency { doorDropdown }
    var normalDoorColor by ColorSetting("Normal Door", Color(107, 58, 17), false, desc = "Color of normal doors.").withDependency { doorDropdown }
    var puzzleDoorColor by ColorSetting("Puzzle Door", Color(117, 0, 133), false, desc = "Color of puzzle doors.").withDependency { doorDropdown }
    var championDoorColor by ColorSetting("Champion Door", Color(254, 223, 0), false, desc = "Color of champion doors.").withDependency { doorDropdown }
    var trapDoorColor by ColorSetting("Trap Door", Color(216, 127, 51), false, desc = "Color of trap doors.").withDependency { doorDropdown }
    var entranceDoorColor by ColorSetting("Entrance Door", Color(20, 133, 0), false, desc = "Color of entrance doors.").withDependency { doorDropdown }
    var fairyDoorColor by ColorSetting("Fairy Door", Color(244, 19, 139), false, desc = "Color of fairy room doors.").withDependency { doorDropdown }
    var rareDoorColor by ColorSetting("Rare Door", Color(255, 203, 89), false, desc = "Color of rare doors.").withDependency { doorDropdown }

    // Room Settings
    private val roomDropdown by DropdownSetting("Room Settings")
    var darkenMultiplier by NumberSetting("Darken Multiplier", 0.4f, 0f, 1f, 0.05f, desc = "Multiplier for darkening rooms.").withDependency { roomDropdown }
    var unopenedRoomColor by ColorSetting("Unopened Room", Color(30, 30, 30), false, desc = "Color of unopened rooms.").withDependency { roomDropdown }
    var bloodRoomColor by ColorSetting("Blood Room", Color(255, 0, 0), false, desc = "Color of blood rooms.").withDependency { roomDropdown }
    var normalRoomColor by ColorSetting("Normal Room", Color(107, 58, 17), false, desc = "Color of normal rooms.").withDependency { roomDropdown }
    var puzzleRoomColor by ColorSetting("Puzzle Room", Color(117, 0, 133), false, desc = "Color of puzzle rooms.").withDependency { roomDropdown }
    var championRoomColor by ColorSetting("Champion Room", Color(254, 223, 0), false, desc = "Color of champion rooms.").withDependency { roomDropdown }
    var trapRoomColor by ColorSetting("Trap Room", Color(216, 127, 51), false, desc = "Color of trap rooms.").withDependency { roomDropdown }
    var entranceRoomColor by ColorSetting("Entrance Room", Color(20, 133, 0), false, desc = "Color of entrance rooms.").withDependency { roomDropdown }
    var fairyRoomColor by ColorSetting("Fairy Room", Color(244, 19, 139), false, desc = "Color of fairy rooms.").withDependency { roomDropdown }
    var rareRoomColor by ColorSetting("Rare Room", Color(255, 203, 89), false, desc = "Color of rare rooms.").withDependency { roomDropdown }

    private val mapHud by HUD("Dungeon Map", "Displays the dungeon map with customizable colors.", false) { example ->
        when {
            DungeonUtils.openRoomCount == 0 && !example -> 0 to 0
            example -> renderExampleMap()
            else -> renderDungeonMap()
        }
    }

    private fun GuiGraphics.renderExampleMap(): Pair<Int, Int> {
        val roomsX = 116
        val roomsZ = 116
        val offset = backgroundSize.toInt()

        fill(0, 0, roomsX + offset * 2, roomsZ + offset * 2, backgroundColor.rgba)
        drawCenteredString(mc.font, "MAP", roomsX / 2 + offset, roomsZ / 2 + offset - mc.font.lineHeight, AwtColor.WHITE.rgb)

        return (roomsX + offset * 2) to (roomsZ + offset * 2)
    }

    private fun GuiGraphics.renderDungeonMap(): Pair<Int, Int> {
        val matrices = pose()
        val mapSize = DungMap.calculateMapSize()
        val roomsX = mapSize.x * 16 + (mapSize.x - 1) * 4
        val roomsZ = mapSize.z * 16 + (mapSize.z - 1) * 4
        val offset = backgroundSize.toInt() * 2

        matrices.pushMatrix()

        fill(0, 0, roomsX + offset, roomsZ + offset, backgroundColor.rgba)
        hollowFill(0, 0, roomsX + offset, roomsZ + offset, 1, Colors.gray26)
        matrices.translate(backgroundSize, backgroundSize)

        for (room in MapScanner.allRooms.values) {
            if (room.state == RoomState.UNDISCOVERED || room.state == RoomState.UNOPENED) continue
            for (tile in room.tiles) renderTile(tile)
        }

        for (door in MapScanner.doors) {
            if (!door.seen) continue
            renderTile(door)
            for (roomTile in door.rooms) {
                if (roomTile.owner.state == RoomState.UNOPENED) renderTile(roomTile)
            }
        }

        val fontHeight = mc.font.lineHeight
        val textFactor = 1 / textScaling

        for ((name, room) in MapScanner.allRooms) {
            if (room.data.type.equalsOneOf(RoomType.FAIRY, RoomType.ENTRANCE, RoomType.BLOOD)) continue
            if (room.state.equalsOneOf(RoomState.UNDISCOVERED, RoomState.UNOPENED)) continue

            val splitName = name.split(" ")
            val defaultHeight = 8 - fontHeight / (2 * textFactor) - ((splitName.size - 1) / 2f * (fontHeight / textFactor)).toInt()
            val placement = room.textPlacement()
            val color = when (room.state) {
                RoomState.GREEN -> Colors.MINECRAFT_GREEN
                RoomState.CLEARED -> Colors.WHITE
                RoomState.DISCOVERED -> Color(100, 100, 100)
                RoomState.FAILED -> Colors.MINECRAFT_RED
                else -> Colors.WHITE
            }.rgba

            for ((index, text) in splitName.withIndex()) {
                matrices.pushMatrix()
                matrices.translate(
                    placement.x + 8f,
                    placement.z + index * (fontHeight / textFactor) + defaultHeight
                )
                matrices.scale(textScaling)
                drawCenteredString(mc.font, text, 0, 0, color)
                matrices.popMatrix()
            }
        }

        if (!DungeonUtils.inBoss) {
            val renderNames = mc.player?.mainHandItem?.itemId?.equalsOneOf("INFINITE_SPIRIT_LEAP", "SPIRIT_LEAP") == true

            for (player in DungeonUtils.dungeonTeammates) {
                if (player.isDead) continue

                val (posX, posZ) = player.mapRenderPosition()

                matrices.pushMatrix()
                matrices.translate(posX, posZ)

                if (renderNames) {
                    matrices.pushMatrix()
                    matrices.scale(playerNamesScaling)
                    drawCenteredString(mc.font, player.name, 0, 8, playerNameColor.rgba)
                    matrices.popMatrix()
                }

                player.locationSkin?.let { skin ->
                    matrices.rotate(Math.toRadians(180.0 + player.mapRenderYaw()).toFloat())

                    if (playerHeadBackgroundSize != 0) {
                        val size = 5 + playerHeadBackgroundSize
                        fill(-size, -size, size, size, player.clazz.color.rgba)
                    }

                    PlayerFaceRenderer.draw(this, skin, -5, -5, 10, false, false, -1)
                }

                matrices.popMatrix()
            }
        }

        matrices.popMatrix()

        return (roomsX + offset) to (roomsZ + offset)
    }

    private fun GuiGraphics.renderTile(tile: Tile) {
        val size = tile.size()
        if (size == Vec2i(0, 0)) return

        val placement = tile.placement()
        val colors = tile.color()

        pose().pushMatrix()
        pose().translate(placement.x.toFloat(), placement.z.toFloat())

        when (colors.size) {
            1 -> fill(0, 0, size.x, size.z, colors[0].rgba)
            2 -> {
                fill(0, 0, 16, 8, colors[0].rgba)
                fill(0, 8, 16, 16, colors[1].rgba)
            }
            3 -> {
                fill(0, 0, size.x, 5, colors[0].rgba)
                fill(0, 0, 5, 10, colors[0].rgba)
                fill(10, 5, 16, 16, colors[1].rgba)
                fill(0, 10, 16, 16, colors[1].rgba)
                fill(5, 5, 11, 11, colors[2].rgba)
            }
        }

        pose().popMatrix()
    }

    init {
        on<WorldEvent.Load> {
            SpecialColumn.unload()
            MapScanner.unload()
            DungMap.unload()
        }

        on<TickEvent.End> {
            MapScanner.scan(world)
        }

        ClientChunkEvents.CHUNK_LOAD.register { _, _ ->
            DungMap.onChunkLoad()
        }

        onReceive<ClientboundMapItemDataPacket> {
            mc.execute { DungMap.rescanMapItem(this) }
        }
    }
}
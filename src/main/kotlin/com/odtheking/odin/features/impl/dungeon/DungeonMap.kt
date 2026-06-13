package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.map.DungMap
import com.odtheking.odin.features.impl.dungeon.map.MapScanner
import com.odtheking.odin.features.impl.dungeon.map.SpecialColumn
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomType
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.PlayerFaceRenderer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.resources.Identifier
import java.awt.Color as AwtColor

object DungeonMap : Module(
    name = "Dungeon Map",
    description = "Customizable dungeon map with room colors, door colors, and player names."
) {
    private val disableBoss by BooleanSetting("Disable in Boss", true, desc = "Disables the map during boss fights.")
    val disablePred by BooleanSetting("Disable Predictions", false, desc = "Disables map predictions for unseen rooms.")

    private val backgroundColor by ColorSetting("Background Color", Color(0, 0, 0, 0.7f), true, desc = "The background color of the map.")
    private val backgroundOutline by ColorSetting("Background Outline", Colors.gray26, false, desc = "The color of the background border.").withDependency { backgroundSize != 0f }
    private val backgroundSize by NumberSetting("Background Size", 5f, 0f, 20f, 1f, desc = "The size of the background border.")

    val textScaling by NumberSetting("Text Scaling", 0.45f, 0.1f, 1f, 0.05f, desc = "Scale of room names.")

    private val playerDropdown by DropdownSetting("Player Settings")
    private val playerHeadBackgroundSize by NumberSetting("Player Head BG Size", 1, 0, 10, 1, desc = "Size of player head background.").withDependency { playerDropdown }
    private val playerNamesScaling by NumberSetting("Player Names Scaling", 0.75f, 0.1f, 2f, 0.05f, desc = "Scale of player names.").withDependency { playerDropdown }
    private val playerNameColor by ColorSetting("Player Name Color", Color(70, 70, 70), false, desc = "Color of player names.").withDependency { playerDropdown }

    private val doorDropdown by DropdownSetting("Door Settings")
    val doorThickness by NumberSetting("Door Thickness", 9, 1, 20, 1, desc = "Thickness of doors on map.").withDependency { doorDropdown }
    val unopenedDoorColor by ColorSetting("Unopened Door", Color(30, 30, 30), false, desc = "Color of unopened doors.").withDependency { doorDropdown }
    val bloodDoorColor by ColorSetting("Blood Door", Colors.MINECRAFT_RED, false, desc = "Color of blood room doors.").withDependency { doorDropdown }
    val witherDoorColor by ColorSetting("Wither Door", Colors.BLACK, false, desc = "Color of wither doors.").withDependency { doorDropdown }
    val normalDoorColor by ColorSetting("Normal Door", Color(107, 58, 17), false, desc = "Color of normal doors.").withDependency { doorDropdown }
    val puzzleDoorColor by ColorSetting("Puzzle Door", Color(117, 0, 133), false, desc = "Color of puzzle doors.").withDependency { doorDropdown }
    val championDoorColor by ColorSetting("Champion Door", Color(254, 223, 0), false, desc = "Color of champion doors.").withDependency { doorDropdown }
    val trapDoorColor by ColorSetting("Trap Door", Color(216, 127, 51), false, desc = "Color of trap doors.").withDependency { doorDropdown }
    val entranceDoorColor by ColorSetting("Entrance Door", Color(20, 133, 0), false, desc = "Color of entrance doors.").withDependency { doorDropdown }
    val fairyDoorColor by ColorSetting("Fairy Door", Color(244, 19, 139), false, desc = "Color of fairy room doors.").withDependency { doorDropdown }
    val rareDoorColor by ColorSetting("Rare Door", Color(255, 203, 89), false, desc = "Color of rare doors.").withDependency { doorDropdown }

    // Room Settings
    private val roomDropdown by DropdownSetting("Room Settings")
    val darkenMultiplier by NumberSetting("Darken Multiplier", 0.4f, 0f, 1f, 0.05f, desc = "Multiplier for darkening rooms.").withDependency { roomDropdown }
    val unopenedRoomColor by ColorSetting("Unopened Room", Color(30, 30, 30), false, desc = "Color of unopened rooms.").withDependency { roomDropdown }
    val bloodRoomColor by ColorSetting("Blood Room", Color(255, 0, 0), false, desc = "Color of blood rooms.").withDependency { roomDropdown }
    val normalRoomColor by ColorSetting("Normal Room", Color(107, 58, 17), false, desc = "Color of normal rooms.").withDependency { roomDropdown }
    val puzzleRoomColor by ColorSetting("Puzzle Room", Color(117, 0, 133), false, desc = "Color of puzzle rooms.").withDependency { roomDropdown }
    val championRoomColor by ColorSetting("Champion Room", Color(254, 223, 0), false, desc = "Color of champion rooms.").withDependency { roomDropdown }
    val trapRoomColor by ColorSetting("Trap Room", Color(216, 127, 51), false, desc = "Color of trap rooms.").withDependency { roomDropdown }
    val entranceRoomColor by ColorSetting("Entrance Room", Color(20, 133, 0), false, desc = "Color of entrance rooms.").withDependency { roomDropdown }
    val fairyRoomColor by ColorSetting("Fairy Room", Color(244, 19, 139), false, desc = "Color of fairy rooms.").withDependency { roomDropdown }
    val rareRoomColor by ColorSetting("Rare Room", Color(255, 203, 89), false, desc = "Color of rare rooms.").withDependency { roomDropdown }

    private val mapHud by HUD("Dungeon Map", "Displays the dungeon map with customizable colors.", false) { example ->
        when {
            (!DungeonUtils.inDungeons || (disableBoss && DungeonUtils.inBoss)) && !example -> 0 to 0
            example -> renderExampleMap()
            else -> renderDungeonMap()
        }
    }

    private val marker = Identifier.fromNamespaceAndPath("odin", "map/marker.png")
    val x = Identifier.fromNamespaceAndPath("odin", "map/x.png")
    val green = Identifier.fromNamespaceAndPath("odin", "map/green.png")
    val white = Identifier.fromNamespaceAndPath("odin", "map/white.png")
    val question = Identifier.fromNamespaceAndPath("odin", "map/question.png")

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
        hollowFill(0, 0, roomsX + offset, roomsZ + offset, 1, backgroundOutline)
        matrices.translate(backgroundSize, backgroundSize)

        MapScanner.rooms.forEach { it.render(this) }

        for (door in MapScanner.doors) {
            if (!door.seen) continue
            door.render(this)
        }

        val textFactor = 1 / textScaling

        MapScanner.rooms.forEach { room ->
            if (room.type == RoomType.ENTRANCE || room.type == RoomType.BLOOD) return@forEach
            room.renderName(this, textFactor)
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

                player.playerSkin?.let { skin ->
                    matrices.rotate(Math.toRadians(180.0 + player.mapRenderYaw()).toFloat())

                    if (player.playerSkin == mc.player?.skin) {
                        blit(RenderPipelines.GUI_TEXTURED, marker, 5, 5, 10f, 10f, -10, -10, 10, 10)
                        return@let
                    }

                    if (playerHeadBackgroundSize != 0) {
                        val size = 5 + playerHeadBackgroundSize
                        fill(-size, -size, size, size, player.clazz.color.rgba)
                    }

                    PlayerFaceRenderer.draw(this, skin, -5, -5, 10)
                }

                matrices.popMatrix()
            }
        }

        matrices.popMatrix()

        return (roomsX + offset) to (roomsZ + offset)
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
            DungMap.rescanMapItem(this)
        }
    }
}
package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.map.scan.DungeonMapScan
import com.odtheking.odin.utils.skyblock.dungeon.map.scan.DungeonWorldScan
import com.odtheking.odin.utils.skyblock.dungeon.map.tile.*
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.PlayerFaceRenderer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

internal const val MAP_PX = 128
private val marker = Identifier.withDefaultNamespace("textures/map/decorations/player.png")

internal fun GuiGraphics.renderMap() {
    pose().pushMatrix()
    pose().translate(DungeonMapScan.startX.toFloat(), DungeonMapScan.startY.toFloat())
    renderRooms()
    renderDoors()
    for (room in DungeonMapScan.rooms) renderRoomText(room)
    renderPlayers()
    pose().popMatrix()
}

private fun GuiGraphics.renderRooms() {
    val rs = DungeonMapScan.roomSize.takeIf { it != -1 } ?: return
    val rg = DungeonMapScan.roomGap

    for (room in DungeonMapScan.rooms) {
        val color = roomTypeColor(room.type).rgba
        val ox = room.position.x * rg
        val oy = room.position.z * rg
        when (val shape = room.shape) {
            RoomShape.OneByOne -> fill(ox, oy, ox + rs, oy + rs, color)

            RoomShape.TwoByOne, RoomShape.ThreeByOne, RoomShape.FourByOne -> {
                val n = shape.segments
                if (room.rotation == RoomRotation.SOUTH) fill(ox, oy, ox + (n - 1) * rg + rs, oy + rs, color)
                else fill(ox, oy, ox + rs, oy + (n - 1) * rg + rs, color)
            }

            RoomShape.TwoByTwo -> fill(ox, oy, ox + rg + rs, oy + rg + rs, color)

            RoomShape.L -> when (room.rotation) {
                RoomRotation.WEST -> {
                    fill(ox,      oy,      ox + rs,      oy + rg + rs, color)
                    fill(ox + rs, oy + rg, ox + rg + rs, oy + rg + rs, color)
                }
                RoomRotation.NORTH -> {
                    fill(ox,      oy,      ox + rg + rs, oy + rs,      color)
                    fill(ox + rg, oy + rs, ox + rg + rs, oy + rg + rs, color)
                }
                else -> {
                    fill(ox, oy,      ox + rg + rs, oy + rs,      color)
                    fill(ox, oy + rs, ox + rs,      oy + rg + rs, color)
                }
            }
        }
    }
}

private fun GuiGraphics.renderDoors() {
    val rs = DungeonMapScan.roomSize.takeIf { it != -1 } ?: return
    val rg = DungeonMapScan.roomGap
    val half = (rs - 8) / 2f
    val unknownColor = DungeonMapModule.unknownRoomColor.rgba

    fun GuiGraphics.stubIfNeeded(index: Int, room: MapScanRoom?) {
        if (room != null && room.checkmark != MapCheckmark.UNDISCOVERED) return
        val pos = DungeonMapScan.tiles[index].position
        fill(pos.x * rg, pos.z * rg, pos.x * rg + rs, pos.z * rg + rs, unknownColor)
    }

    for ((_, door) in DungeonMapScan.doors) {
        val originRoom = DungeonMapScan.tiles[door.originTileIndex].room
        val destRoom   = DungeonMapScan.tiles[door.destinationTileIndex].room
        val offset     = door.rotation.offset

        pose().pushMatrix()
        pose().translate(
            door.position.x * rg + offset.x * rs + offset.z * half,
            door.position.z * rg + offset.z * rs + offset.x * half
        )
        fill(0, 0, 4 + 4 * offset.z, 4 + 4 * offset.x, doorColor(door.type, originRoom, destRoom).rgba)
        pose().popMatrix()

        stubIfNeeded(door.originTileIndex, originRoom)
        stubIfNeeded(door.destinationTileIndex, destRoom)
    }
}

private fun GuiGraphics.renderRoomText(room: MapScanRoom) {
    if (room.type.equalsOneOf(RoomType.UNDISCOVERED, RoomType.FAIRY, RoomType.ENTRANCE, RoomType.BLOOD)) return
    if (room.checkmark == MapCheckmark.UNDISCOVERED) return

    val worldRoom = DungeonWorldScan.tiles.getOrNull(room.position.x + room.position.z * 6)?.room
    val name = worldRoom?.name?.takeIf { it.isNotEmpty() } ?: return

    val textColor = when (room.checkmark) {
        MapCheckmark.GREEN -> Colors.MINECRAFT_GREEN
        MapCheckmark.WHITE -> Colors.WHITE
        MapCheckmark.RED   -> Colors.MINECRAFT_RED
        else               -> Color(100, 100, 100)
    }.rgba

    val (cx, cz) = textCenter(room)
    val lines  = name.split(" ")
    val fontH  = mc.font.lineHeight
    val totalH = (lines.size - 1) * fontH * DungeonMapModule.textScaling

    for ((i, line) in lines.withIndex()) {
        pose().pushMatrix()
        pose().translate(cx, cz - totalH / 2f + i * fontH * DungeonMapModule.textScaling)
        pose().scale(DungeonMapModule.textScaling)
        drawCenteredString(mc.font, line, 0, -fontH / 2, textColor)
        pose().popMatrix()
    }
}

private fun GuiGraphics.renderPlayers() {
    if (DungeonUtils.inBoss) return

    val showNames = mc.player?.mainHandItem?.itemId?.equalsOneOf("INFINITE_SPIRIT_LEAP", "SPIRIT_LEAP") == true
    val selfName = mc.player?.name?.string

    for (player in DungeonUtils.dungeonTeammates) {
        if (player.isDead) continue
        val (px, pz) = DungeonMapScan.playerRenderPosition(player.entity, player.mapPos)

        pose().pushMatrix()
        pose().translate(px, pz)

        if (showNames) {
            pose().pushMatrix()
            pose().scale(DungeonMapModule.playerNamesScaling)
            drawCenteredString(mc.font, player.name, 0, 8, DungeonMapModule.playerNameColor.rgba)
            pose().popMatrix()
        }

        pose().rotate(Math.toRadians(180.0 + player.mapRenderYaw()).toFloat())

        val isSelf = player.name == selfName
        if (isSelf && DungeonMapModule.selfVanillaMarker) blit(RenderPipelines.GUI_TEXTURED, marker, -2, -3, 2f, 0f, 5, 7, 8, 8)
        else player.playerSkin?.let { skin ->
            fill(-5, -5, 5, 5, player.clazz.color.rgba)
            PlayerFaceRenderer.draw(this, skin, -4, -4, 8)
        }

        pose().popMatrix()
    }
}

private fun textCenter(room: RoomInfo): Pair<Float, Float> {
    val rs = DungeonMapScan.roomSize.takeIf { it != -1 } ?: return 0f to 0f
    val rg = DungeonMapScan.roomGap
    val half = rs / 2

    fun tileCenter(tile: ScanTile) = tile.position.x * rg + half to tile.position.z * rg + half

    val rotation = room.rotation
    if (rotation == null) {
        val tile = room.segments.minBy { it.position.x * 1000 + it.position.z }
        return tileCenter(tile).let { it.first.toFloat() to it.second.toFloat() }
    }

    val centers = room.segments.map { tileCenter(it) }
    val x = (centers.minOf { it.first } + centers.maxOf { it.first }) / 2f
    val z = when (room.shape) {
        RoomShape.L ->
            if (rotation == RoomRotation.NORTH || rotation == RoomRotation.SOUTH) centers.minOf { it.second }.toFloat()
            else centers.maxOf { it.second }.toFloat()
        else ->
            (centers.minOf { it.second } + centers.maxOf { it.second }) / 2f
    }
    return x to z
}

private fun roomTypeColor(type: RoomType): Color = when (type) {
    RoomType.NORMAL   -> DungeonMapModule.normalRoomColor
    RoomType.PUZZLE   -> DungeonMapModule.puzzleRoomColor
    RoomType.TRAP     -> DungeonMapModule.trapRoomColor
    RoomType.BLOOD    -> DungeonMapModule.bloodRoomColor
    RoomType.ENTRANCE -> DungeonMapModule.entranceRoomColor
    RoomType.FAIRY    -> DungeonMapModule.fairyRoomColor
    RoomType.CHAMPION -> DungeonMapModule.championRoomColor
    else              -> Color(60, 60, 60)
}

private fun doorTypeColor(type: DoorType): Color = when (type) {
    DoorType.Normal -> DungeonMapModule.normalDoorColor
    DoorType.Wither -> DungeonMapModule.witherDoorColor
    DoorType.Blood  -> DungeonMapModule.bloodDoorColor
    DoorType.Fairy  -> DungeonMapModule.fairyDoorColor
}

private fun doorColor(type: DoorType, originRoom: MapScanRoom?, destRoom: MapScanRoom?): Color {
    if (type != DoorType.Normal) return doorTypeColor(type)
    val nonNormal = sequenceOf(originRoom, destRoom).filterNotNull()
        .firstOrNull { it.type != RoomType.NORMAL && it.type != RoomType.UNDISCOVERED }
    return if (nonNormal != null) roomTypeColor(nonNormal.type) else doorTypeColor(DoorType.Normal)
}

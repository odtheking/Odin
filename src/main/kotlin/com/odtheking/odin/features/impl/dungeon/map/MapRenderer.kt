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
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.PlayerFaceExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

internal const val MAP_PX = 128
private val marker = Identifier.withDefaultNamespace("textures/map/decorations/frame.png")
private val cross = Identifier.fromNamespaceAndPath("odin", "textures/map/cross.png")
private val green = Identifier.fromNamespaceAndPath("odin", "textures/map/green_check.png")
private val white = Identifier.fromNamespaceAndPath("odin", "textures/map/white_check.png")
private val question = Identifier.fromNamespaceAndPath("odin", "textures/map/question.png")

internal fun GuiGraphicsExtractor.renderMap() {
    val roomSize = DungeonMapScan.roomSize.takeIf { it != -1 } ?: return
    val roomGap = DungeonMapScan.roomGap

    pose().pushMatrix()
    pose().translate(DungeonMapScan.startX.toFloat(), DungeonMapScan.startY.toFloat())
    renderRooms(roomSize, roomGap)
    renderDoors()
    for (room in DungeonMapScan.rooms) renderRoomText(room)
    renderPlayers()
    pose().popMatrix()
}

private fun GuiGraphicsExtractor.renderRooms(roomSize: Int, roomGap: Int) {
    for (room in DungeonMapScan.rooms) {
        if (room.discovered || room.checkmark != MapCheckmark.UNDISCOVERED) fillRoom(room, roomTypeColor(room.type).rgba, roomSize, roomGap)
    }
}

private fun GuiGraphicsExtractor.fillRoom(room: RoomInfo, color: Int, rs: Int, rg: Int) {
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

private fun DungeonRoom?.isDiscovered(): Boolean =
    this?.discovered == true || this?.checkmark?.let { it != MapCheckmark.UNDISCOVERED } == true

private fun GuiGraphicsExtractor.renderDoors() {
    val rs = DungeonMapScan.roomSize.takeIf { it != -1 } ?: return
    val rg = DungeonMapScan.roomGap
    val half = (rs - 8) / 2f

    for ((_, door) in DungeonMapScan.doors) {
        val originTile = DungeonWorldScan.tiles[door.originTileIndex]
        val destTile   = DungeonWorldScan.tiles[door.destinationTileIndex]
        val originRoom = originTile.room
        val destRoom   = destTile.room

        if (!originRoom.isDiscovered() && !destRoom.isDiscovered()) continue

        val color =
            when (door.type) {
                DoorType.Wither -> DungeonMap.witherDoorColor
                DoorType.Blood  -> DungeonMap.bloodDoorColor
                DoorType.Fairy  -> DungeonMap.fairyDoorColor
                else -> {
                    if (!originRoom.isDiscovered() || !destRoom.isDiscovered()) DungeonMap.unknownRoomColor
                    else listOfNotNull(originRoom, destRoom).firstOrNull { it.type != RoomType.NORMAL }?.type?.let { room ->
                        roomTypeColor(room)
                    } ?: DungeonMap.normalDoorColor
                }
            }

        (if (!originRoom.isDiscovered()) originTile else if (!destRoom.isDiscovered()) destTile else null)?.let { unknownTile ->
            pose().pushMatrix()
            pose().translate((unknownTile.position.x * rg).toFloat(), (unknownTile.position.z * rg).toFloat())
            renderUnknown()
            pose().popMatrix()
        }

        val offset = door.rotation.offset

        pose().pushMatrix()
        pose().translate(
            door.position.x * rg + offset.x * rs + offset.z * half,
            door.position.z * rg + offset.z * rs + offset.x * half
        )
        fill(0, 0, 4 + 4 * offset.z, 4 + 4 * offset.x, color.rgba)
        pose().popMatrix()
    }
}

fun GuiGraphicsExtractor.renderUnknown() {
    val rs = DungeonMapScan.roomSize.takeIf { it != -1 } ?: return
    // can easily add here room guessing
    fill(0, 0, rs, rs, DungeonMap.unknownRoomColor.rgba)
    blit(RenderPipelines.GUI_TEXTURED, question, 0, 0, rs.toFloat(), rs.toFloat(), rs, rs, rs, rs)
}

private fun GuiGraphicsExtractor.renderRoomText(room: DungeonRoom) {
    if (room.type.equalsOneOf(RoomType.UNDISCOVERED, RoomType.FAIRY, RoomType.ENTRANCE, RoomType.BLOOD)) return
    val scannedRoom = DungeonWorldScan.tiles.getOrNull(room.position.x + room.position.z * 6)?.room
    val rs = DungeonMapScan.roomSize.takeIf { it != -1 } ?: return

    if (scannedRoom?.discovered == false) {
        when (room.checkmark) {
            MapCheckmark.GREEN -> green
            MapCheckmark.WHITE -> white
            MapCheckmark.RED -> cross
            else -> null
        }?.let { texture ->
            val (iconX, iconY) = roomTopLeftSegmentIconPosition(room, rs)
            blit(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, rs.toFloat(), rs.toFloat(), rs, rs, rs, rs)
        }
        return
    }

    val (cx, cz) = textCenter(room)
    val fontH  = mc.font.lineHeight

    val textColor = when (room.checkmark) {
        MapCheckmark.GREEN -> Colors.MINECRAFT_GREEN
        MapCheckmark.WHITE -> Colors.WHITE
        MapCheckmark.RED   -> Colors.MINECRAFT_RED
        else               -> Color(100, 100, 100)
    }.rgba

    val lines  = scannedRoom?.name?.split(" ") ?: return
    val totalH = (lines.size - 1) * fontH * DungeonMap.textScaling

    for ((i, line) in lines.withIndex()) {
        pose().pushMatrix()
        pose().translate(cx, cz - totalH / 2f + i * fontH * DungeonMap.textScaling)
        pose().scale(DungeonMap.textScaling)
        centeredText(mc.font, line, 0, -fontH / 2, textColor)
        pose().popMatrix()
    }
}

private fun GuiGraphicsExtractor.renderPlayers() {
    val showNames = mc.player?.mainHandItem?.itemId?.equalsOneOf("INFINITE_SPIRIT_LEAP", "SPIRIT_LEAP") == true
    val selfName = mc.player?.name?.string

    for (player in DungeonUtils.dungeonTeammates) {
        if (player.isDead) continue
        val (px, pz) = DungeonMapScan.playerRenderPosition(player.entity, player.mapPos)

        pose().pushMatrix()
        pose().translate(px, pz)

        if (showNames) {
            pose().pushMatrix()
            pose().scale(DungeonMap.playerNamesScaling)
            centeredText(mc.font, player.name, 0, 8, DungeonMap.playerNameColor.rgba)
            pose().popMatrix()
        }

        pose().rotate(Math.toRadians(180.0 + player.renderYaw).toFloat())

        val isSelf = player.name == selfName
        if (isSelf && DungeonMap.selfVanillaMarker) blit(RenderPipelines.GUI_TEXTURED, marker, -2, -3, 2f, 0f, 5, 7, 8, 8)
        else player.playerSkin?.let { skin ->
            fill(-5, -5, 5, 5, player.clazz.color.rgba)
            PlayerFaceExtractor.extractRenderState(this, skin, -4, -4, 8)
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
    RoomType.NORMAL   -> DungeonMap.normalRoomColor
    RoomType.PUZZLE   -> DungeonMap.puzzleRoomColor
    RoomType.TRAP     -> DungeonMap.trapRoomColor
    RoomType.BLOOD    -> DungeonMap.bloodRoomColor
    RoomType.ENTRANCE -> DungeonMap.entranceRoomColor
    RoomType.FAIRY    -> DungeonMap.fairyRoomColor
    RoomType.CHAMPION -> DungeonMap.championRoomColor
    RoomType.RARE     -> DungeonMap.normalRoomColor
    else              -> Color(60, 60, 60)
}

private fun roomTopLeftSegmentIconPosition(room: RoomInfo, iconSize: Int): Pair<Int, Int> {
    val rs = DungeonMapScan.roomSize.takeIf { it != -1 } ?: return 0 to 0
    val rg = DungeonMapScan.roomGap
    return room.position.x * rg + (rs - iconSize) / 2 to room.position.z * rg + (rs - iconSize) / 2
}

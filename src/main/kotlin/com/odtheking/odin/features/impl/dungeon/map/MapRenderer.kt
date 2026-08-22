package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.map.DungeonScan.ROOM_SPACING
import com.odtheking.odin.features.impl.dungeon.map.DungeonScan.startX
import com.odtheking.odin.features.impl.dungeon.map.DungeonScan.startY
import com.odtheking.odin.features.impl.dungeon.map.tile.*
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.PlayerFaceExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player

private val marker = Identifier.withDefaultNamespace("textures/map/decorations/frame.png")
private val cross = Identifier.fromNamespaceAndPath("odin", "map/cross.png")
private val green = Identifier.fromNamespaceAndPath("odin", "map/green_check.png")
private val white = Identifier.fromNamespaceAndPath("odin", "map/white_check.png")
private val question = Identifier.fromNamespaceAndPath("odin", "map/question.png")

private const val MAP_ROOM_SIZE = 16
private const val MAP_ROOM_GAP = MAP_ROOM_SIZE + ROOM_SPACING

fun GuiGraphicsExtractor.fillRoom(room: DungeonRoom, color: Int) {
    val ox = room.topLeft.x * MAP_ROOM_GAP
    val oy = room.topLeft.z * MAP_ROOM_GAP
    when (val shape = room.shape) {
        RoomShape.OneByOne -> fill(ox, oy, ox + MAP_ROOM_SIZE, oy + MAP_ROOM_SIZE, color)
        RoomShape.TwoByTwo -> fill(ox, oy, ox + MAP_ROOM_GAP + MAP_ROOM_SIZE, oy + MAP_ROOM_GAP + MAP_ROOM_SIZE, color)

        RoomShape.L -> when (room.rotation) {
            RoomRotation.WEST -> {
                fill(ox, oy,      ox + MAP_ROOM_GAP + MAP_ROOM_SIZE, oy + MAP_ROOM_SIZE, color)
                fill(ox, oy + MAP_ROOM_SIZE, ox + MAP_ROOM_SIZE, oy + MAP_ROOM_GAP + MAP_ROOM_SIZE, color)
            }
            RoomRotation.NORTH -> {
                fill(ox,      oy,      ox + MAP_ROOM_GAP + MAP_ROOM_SIZE, oy + MAP_ROOM_SIZE, color)
                fill(ox + MAP_ROOM_GAP, oy + MAP_ROOM_SIZE, ox + MAP_ROOM_GAP + MAP_ROOM_SIZE, oy + MAP_ROOM_GAP + MAP_ROOM_SIZE, color)
            }
            else -> {
                fill(ox,      oy,      ox + MAP_ROOM_SIZE,      oy + MAP_ROOM_GAP + MAP_ROOM_SIZE, color)
                fill(ox + MAP_ROOM_SIZE, oy + MAP_ROOM_GAP, ox + MAP_ROOM_GAP + MAP_ROOM_SIZE, oy + MAP_ROOM_GAP + MAP_ROOM_SIZE, color)
            }
        }

        else -> {
            if (room.rotation == RoomRotation.SOUTH) fill(ox, oy, ox + (shape.tileAmount - 1) * MAP_ROOM_GAP + MAP_ROOM_SIZE, oy + MAP_ROOM_SIZE, color)
            else fill(ox, oy, ox + MAP_ROOM_SIZE, oy + (shape.tileAmount - 1) * MAP_ROOM_GAP + MAP_ROOM_SIZE, color)
        }
    }
}

fun GuiGraphicsExtractor.renderDoors(doors: Collection<DungeonDoor>) {
    val rg = MAP_ROOM_GAP
    val half = (MAP_ROOM_SIZE - 8) / 2f

    for (door in doors) {
        val offset = door.rotation.offset

        pose().pushMatrix()
        pose().translate(
            door.position.x * rg + offset.x * MAP_ROOM_SIZE + offset.z * half,
            door.position.z * rg + offset.z * MAP_ROOM_SIZE + offset.x * half
        )
        fill(0, 0, 4 + 4 * offset.z, 4 + 4 * offset.x, door.color.rgba)
        pose().popMatrix()
    }
}

fun GuiGraphicsExtractor.renderPathHints(pathHints: Collection<DungeonTile>) {
    for ((position, room) in pathHints) {
        val (x, y) = position.x * MAP_ROOM_GAP to position.z * MAP_ROOM_GAP

        val colors = if (room?.type == RoomType.BLOOD) arrayOf(DungeonMap.bloodRoomColor.darker(0.5f))
        else if (!DungeonMap.disablePred && room?.isKnown1x1 == true) SpecialColumn.colorGuessForUnknown(position.x)
        else arrayOf(DungeonMap.unknownRoomColor)

        when (colors.size) {
            1 -> fill(x, y, x + MAP_ROOM_SIZE, y + MAP_ROOM_SIZE, colors[0].rgba)

            2 -> {
                val half = MAP_ROOM_SIZE / 2
                fill(x, y, x + half, y + MAP_ROOM_SIZE, colors[0].darker(0.5f).rgba)
                fill(x + half, y, x + MAP_ROOM_SIZE, y + MAP_ROOM_SIZE, colors[1].darker(0.5f).rgba)
            }

            3 -> {
                val third = MAP_ROOM_SIZE / 3
                fill(x, y, x + third, y + MAP_ROOM_SIZE, colors[0].darker(0.5f).rgba)
                fill(x + third, y, x + third * 2, y + MAP_ROOM_SIZE, colors[1].darker(0.5f).rgba)
                fill(x + third * 2, y, x + MAP_ROOM_SIZE, y + MAP_ROOM_SIZE, colors[2].darker(0.5f).rgba)
            }
        }
        renderIcon(IVec2(x, y), question)
    }
}

fun GuiGraphicsExtractor.renderIcon(pos: IVec2, identifier: Identifier) {
    val size = MAP_ROOM_SIZE - ROOM_SPACING
    blit(RenderPipelines.GUI_TEXTURED, identifier, pos.x + 2, pos.z + 2, size.toFloat(), size.toFloat(), size, size, size, size)
}

fun GuiGraphicsExtractor.renderRoomText(room: DungeonRoom) {
    if (room.type.equalsOneOf(RoomType.UNDISCOVERED, RoomType.FAIRY, RoomType.ENTRANCE, RoomType.BLOOD)) return

    if (!room.walkedInto) {
        when (room.checkmark) {
            MapCheckmark.GREEN -> green
            MapCheckmark.WHITE -> white
            MapCheckmark.RED -> cross
            else -> null
        }?.let { texture -> renderIcon(room.topLeft * MAP_ROOM_GAP, texture) }
        return
    }

    val (cx, cz) = room.center?.let { it.x to it.z } ?: return
    val fontH  = mc.font.lineHeight

    val textColor = when (room.checkmark) {
        MapCheckmark.GREEN -> Colors.MINECRAFT_GREEN
        MapCheckmark.WHITE -> Colors.WHITE
        MapCheckmark.RED   -> Colors.MINECRAFT_RED
        else               -> Color(100, 100, 100)
    }.rgba

    val secretsLine = if ((room.data?.maxSecrets ?: 0) > 0) " ${room.foundSecrets ?: "?"}/${room.data?.maxSecrets}" else ""
    val lines = when (DungeonMap.roomText) {
        0 -> "${room.name}$secretsLine"
        1 -> room.name
        else -> secretsLine
    }?.trim()?.split(" ") ?: return
    val totalH = (lines.size - 1) * fontH * DungeonMap.textScaling

    for ((i, line) in lines.withIndex()) {
        pose().pushMatrix()
        pose().translate(cx.toFloat(), cz.toFloat() - totalH / 2f + i * fontH * DungeonMap.textScaling)
        pose().scale(DungeonMap.textScaling)
        centeredText(mc.font, line, 0, -fontH / 2, textColor)
        pose().popMatrix()
    }
}

private fun playerRenderPosition(entity: Player?, mapPos: IVec2): Pair<Float, Float> {
    entity?.let {
        val mapX = (it.renderX.toFloat() + 200f) * MAP_ROOM_GAP / 32f
        val mapZ = (it.renderZ.toFloat() + 200f) * MAP_ROOM_GAP / 32f
        return mapX to mapZ
    }

    val pixelX = (mapPos.x + 128) / 2f - startX
    val pixelY = (mapPos.z + 128) / 2f - startY
    return pixelX to pixelY
}

fun GuiGraphicsExtractor.renderPlayers() {
    val showNames = mc.player?.mainHandItem?.itemId?.equalsOneOf("INFINITE_SPIRIT_LEAP", "SPIRIT_LEAP") == true

    for (player in DungeonUtils.dungeonTeammatesNoSelf) {
        if (player.isDead) continue
        val (px, pz) = playerRenderPosition(player.entity, player.mapPos)

        pose().pushMatrix()
        pose().translate(px, pz)

        if (showNames) {
            pose().pushMatrix()
            pose().scale(DungeonMap.playerNamesScaling)
            centeredText(mc.font, player.name, 0, 8, DungeonMap.playerNameColor.rgba)
            pose().popMatrix()
        }

        pose().rotate(Math.toRadians(180.0 + player.renderYaw).toFloat())

        player.playerSkin?.let { skin ->
            fill(-5, -5, 5, 5, player.clazz.color.rgba)
            PlayerFaceExtractor.extractRenderState(this, skin, -4, -4, 8)
        }

        pose().popMatrix()
    }

    pose().pushMatrix()
    val (selfX, selfZ) = playerRenderPosition(mc.player, IVec2(0, 0))
    pose().translate(selfX, selfZ)
    pose().rotate(Math.toRadians(180.0 + (mc.player?.yRot ?: 0f)).toFloat())

    if (DungeonMap.playerHead) {
        mc.player?.skin?.let { skin ->
            fill(-5, -5, 5, 5, DungeonUtils.currentDungeonPlayer.clazz.color.rgba)
            PlayerFaceExtractor.extractRenderState(this, skin, -4, -4, 8)
        }
    } else blit(RenderPipelines.GUI_TEXTURED, marker, -2, -3, 2f, 0f, 5, 7, 8, 8)
    pose().popMatrix()
}

fun GuiGraphicsExtractor.renderMap(rooms: Collection<DungeonRoom>, doors: Collection<DungeonDoor>, pathHints: Collection<DungeonTile>) {
    renderDoors(doors)
    for (room in rooms) if (room.isViewable) fillRoom(room, roomTypeColor(room.type).rgba)
    for (room in rooms) renderRoomText(room)
    renderPathHints(pathHints)
}

fun buildExampleRooms(): List<DungeonRoom> {
    fun room(type: RoomType, topLeft: IVec2, rotation: RoomRotation?, checkmark: MapCheckmark, walkedInto: Boolean, data: RoomData? = null) =
        DungeonRoom(type, topLeft, data).apply {
            data?.let { shape = it.shape }
            this.rotation = rotation
            this.checkmark = checkmark
            this.walkedInto = walkedInto
        }

    return listOf(
        room(RoomType.ENTRANCE, IVec2(2, 0), RoomRotation.NORTH, MapCheckmark.GREEN, true),
        room(RoomType.NORMAL,   IVec2(0, 0), RoomRotation.WEST,  MapCheckmark.NONE,  true, RoomData.getRoomData(1051405699)), // hallway
        room(RoomType.PUZZLE,   IVec2(1, 0), RoomRotation.NORTH, MapCheckmark.NONE,  true, RoomData.getRoomData(379499781)), // water board
        room(RoomType.NORMAL,   IVec2(3, 0), RoomRotation.WEST,  MapCheckmark.WHITE, true, RoomData.getRoomData(90256084)), // waterfall
        room(RoomType.NORMAL,   IVec2(4, 0), RoomRotation.SOUTH, MapCheckmark.NONE,  true, RoomData.getRoomData(1024359556)), // bridges
        room(RoomType.NORMAL,   IVec2(1, 1), RoomRotation.SOUTH, MapCheckmark.WHITE, true, RoomData.getRoomData(1250712883)), // museum
        room(RoomType.NORMAL,   IVec2(4, 1), RoomRotation.SOUTH, MapCheckmark.NONE,  true, RoomData.getRoomData(950592972)), // cathedral
        room(RoomType.NORMAL,   IVec2(1, 3), RoomRotation.NORTH, MapCheckmark.WHITE, true, RoomData.getRoomData(-333637832)), // water
        room(RoomType.FAIRY,    IVec2(2, 3), RoomRotation.SOUTH, MapCheckmark.GREEN, true),
        room(RoomType.CHAMPION, IVec2(4, 3), RoomRotation.SOUTH, MapCheckmark.GREEN,  false, RoomData.getRoomData(-1334473473)), // dragon
        room(RoomType.TRAP,     IVec2(5, 3), RoomRotation.NORTH, MapCheckmark.GREEN, false, RoomData.getRoomData(1590699551)), // old trap
        room(RoomType.PUZZLE,   IVec2(0, 4), RoomRotation.WEST,  MapCheckmark.NONE,  true, RoomData.getRoomData(799715466)), // tp maze
        room(RoomType.NORMAL,   IVec2(1, 4), RoomRotation.EAST,  MapCheckmark.WHITE, true, RoomData.getRoomData(1484939648)), // spikes
        room(RoomType.NORMAL,   IVec2(2, 4), RoomRotation.WEST,  MapCheckmark.WHITE, true, RoomData.getRoomData(-1764045332)), // beams
        room(RoomType.NORMAL,   IVec2(3, 4), RoomRotation.NORTH, MapCheckmark.NONE,  false, RoomData.getRoomData(76347246)), // mirror
        room(RoomType.NORMAL,   IVec2(4, 4), RoomRotation.WEST,  MapCheckmark.NONE,  false, RoomData.getRoomData(-1899702429)), // silver sword
        room(RoomType.NORMAL,   IVec2(5, 4), RoomRotation.WEST,  MapCheckmark.WHITE,  false, RoomData.getRoomData(-151940807)), // staircase
        room(RoomType.NORMAL,   IVec2(0, 5), RoomRotation.SOUTH, MapCheckmark.WHITE, true, RoomData.getRoomData(259768244)), // archway
        room(RoomType.BLOOD,    IVec2(2, 5), RoomRotation.EAST,  MapCheckmark.NONE,  true),
        room(RoomType.NORMAL,   IVec2(3, 5), RoomRotation.SOUTH, MapCheckmark.NONE,  false, RoomData.getRoomData(222124420)), // wizard
    )
}

fun buildExampleDoors(): List<DungeonDoor> = listOf(
    DungeonDoor(IVec2(2, 0), DoorRotation.Vertical, DoorType.Normal, DungeonMap.entranceRoomColor),
    DungeonDoor(IVec2(1, 0), DoorRotation.Vertical, DoorType.Normal, DungeonMap.puzzleRoomColor),
    DungeonDoor(IVec2(0, 1), DoorRotation.Horizontal, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(2, 2), DoorRotation.Horizontal, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(1, 2), DoorRotation.Vertical, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(4, 0), DoorRotation.Vertical, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(3, 1), DoorRotation.Horizontal, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(3, 3), DoorRotation.Vertical, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(1, 3), DoorRotation.Horizontal, DoorType.Fairy, DungeonMap.fairyDoorColor),
    DungeonDoor(IVec2(2, 3), DoorRotation.Vertical, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(4, 3), DoorRotation.Vertical, DoorType.Normal, DungeonMap.championRoomColor),
    DungeonDoor(IVec2(5, 3), DoorRotation.Vertical, DoorType.Normal, DungeonMap.trapRoomColor),
    DungeonDoor(IVec2(0, 4), DoorRotation.Horizontal, DoorType.Normal, DungeonMap.puzzleRoomColor),
    DungeonDoor(IVec2(1, 4), DoorRotation.Horizontal, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(1, 4), DoorRotation.Vertical, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(3, 4), DoorRotation.Horizontal, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(3, 4), DoorRotation.Vertical, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(5, 4), DoorRotation.Vertical, DoorType.Normal, DungeonMap.normalDoorColor),
    DungeonDoor(IVec2(1, 5), DoorRotation.Horizontal, DoorType.Blood, DungeonMap.bloodDoorColor)
)

fun roomTypeColor(type: RoomType): Color = when (type) {
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
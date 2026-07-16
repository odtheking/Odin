package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.map.tile.*
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.PlayerFaceExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

private val marker = Identifier.withDefaultNamespace("textures/map/decorations/frame.png")
private val cross = Identifier.fromNamespaceAndPath("odin", "map/cross.png")
private val green = Identifier.fromNamespaceAndPath("odin", "map/green_check.png")
private val white = Identifier.fromNamespaceAndPath("odin", "map/white_check.png")
private val question = Identifier.fromNamespaceAndPath("odin", "map/question.png")

internal fun GuiGraphicsExtractor.renderMap() {
    pose().pushMatrix()
    pose().translate(DungeonScan.startX.toFloat(), DungeonScan.startY.toFloat())

    renderDoors()

    for (room in DungeonScan.rooms) {
        if (room.isViewable) fillRoom(room, roomTypeColor(room.type).rgba, DungeonScan.roomSize, DungeonScan.roomGap)
    }

    for (room in DungeonScan.rooms) renderRoomText(room)

    if (!DungeonUtils.inBoss) renderPlayers()

    pose().popMatrix()
}

private fun GuiGraphicsExtractor.fillRoom(room: DungeonRoom, color: Int, rs: Int, rg: Int) {
    val ox = room.topLeft.x * rg
    val oy = room.topLeft.z * rg
    when (val shape = room.shape) {
        RoomShape.OneByOne -> fill(ox, oy, ox + rs, oy + rs, color)
        RoomShape.TwoByTwo -> fill(ox, oy, ox + rg + rs, oy + rg + rs, color)

        RoomShape.L -> when (room.rotation) {
            RoomRotation.WEST -> {
                fill(ox, oy,      ox + rg + rs, oy + rs,      color)
                fill(ox, oy + rs, ox + rs,      oy + rg + rs, color)
            }
            RoomRotation.NORTH -> {
                fill(ox,      oy,      ox + rg + rs, oy + rs,      color)
                fill(ox + rg, oy + rs, ox + rg + rs, oy + rg + rs, color)
            }
            else -> {
                fill(ox,      oy,      ox + rs,      oy + rg + rs, color)
                fill(ox + rs, oy + rg, ox + rg + rs, oy + rg + rs, color)
            }
        }

        else -> {
            if (room.rotation == RoomRotation.SOUTH) fill(ox, oy, ox + (shape.tileAmount - 1) * rg + rs, oy + rs, color)
            else fill(ox, oy, ox + rs, oy + (shape.tileAmount - 1) * rg + rs, color)
        }
    }
}

private fun GuiGraphicsExtractor.renderDoors() {
    val rs = DungeonScan.roomSize
    val rg = DungeonScan.roomGap
    val half = (rs - 8) / 2f

    for ((_, door) in DungeonScan.doors) {
        val offset = door.rotation.offset

        pose().pushMatrix()
        pose().translate(
            door.position.x * rg + offset.x * rs + offset.z * half,
            door.position.z * rg + offset.z * rs + offset.x * half
        )
        fill(0, 0, 4 + 4 * offset.z, 4 + 4 * offset.x, door.color.rgba)
        pose().popMatrix()
    }

    for (unknownTile in DungeonScan.pathHints) {
        val (x, y) = unknownTile.position.x * rg to unknownTile.position.z * rg
        val rs = DungeonScan.roomSize

        val colors = if (unknownTile.room?.type == RoomType.BLOOD) arrayOf(DungeonMap.bloodRoomColor.darker(0.5f))
        else if (!DungeonMap.disablePred && unknownTile.room?.isKnown1x1 == true) SpecialColumn.colorGuessForUnknown(unknownTile.position.x)
        else arrayOf(DungeonMap.unknownRoomColor)

        when (colors.size) {
            1 -> fill(x, y, x + rs, y + rs, colors[0].rgba)

            2 -> {
                val half = rs / 2
                fill(x, y, x + half, y + rs, colors[0].darker(0.5f).rgba)
                fill(x + half, y, x + rs, y + rs, colors[1].darker(0.5f).rgba)
            }

            3 -> {
                val third = rs / 3
                fill(x, y, x + third, y + rs, colors[0].darker(0.5f).rgba)
                fill(x + third, y, x + third * 2, y + rs, colors[1].darker(0.5f).rgba)
                fill(x + third * 2, y, x + rs, y + rs, colors[2].darker(0.5f).rgba)
            }
        }
        renderIcon(IVec2(x, y), question)
    }
}

fun GuiGraphicsExtractor.renderIcon(pos: IVec2, identifier: Identifier) {
    val rs = DungeonScan.roomSize - 4
    blit(RenderPipelines.GUI_TEXTURED, identifier, pos.x + 2, pos.z + 2, rs.toFloat(), rs.toFloat(), rs, rs, rs, rs)
}

private fun GuiGraphicsExtractor.renderRoomText(room: DungeonRoom) {
    if (room.type.equalsOneOf(RoomType.UNDISCOVERED, RoomType.FAIRY, RoomType.ENTRANCE, RoomType.BLOOD)) return
    val scannedRoom = DungeonScan.tiles.getOrNull(room.topLeft.x + room.topLeft.z * 6)?.room

    if (scannedRoom?.walkedInto != true) {
        when (room.checkmark) {
            MapCheckmark.GREEN -> green
            MapCheckmark.WHITE -> white
            MapCheckmark.RED -> cross
            else -> null
        }?.let { texture -> renderIcon(room.topLeft * DungeonScan.roomGap, texture) }
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

    val lines  = scannedRoom.name?.split(" ") ?: return
    val totalH = (lines.size - 1) * fontH * DungeonMap.textScaling

    for ((i, line) in lines.withIndex()) {
        pose().pushMatrix()
        pose().translate(cx.toFloat(), cz.toFloat() - totalH / 2f + i * fontH * DungeonMap.textScaling)
        pose().scale(DungeonMap.textScaling)
        centeredText(mc.font, line, 0, -fontH / 2, textColor)
        pose().popMatrix()
    }
}

private fun GuiGraphicsExtractor.renderPlayers() {
    val showNames = mc.player?.mainHandItem?.itemId?.equalsOneOf("INFINITE_SPIRIT_LEAP", "SPIRIT_LEAP") == true

    for (player in DungeonUtils.dungeonTeammatesNoSelf) {
        if (player.isDead) continue
        val (px, pz) = DungeonScan.playerRenderPosition(player.entity, player.mapPos)

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
    val (selfX, selfZ) = DungeonScan.playerRenderPosition(mc.player, IVec2(0, 0))
    pose().translate(selfX, selfZ)
    pose().rotate(Math.toRadians(180.0 + (mc.player?.yRot ?: 0f)).toFloat())
    DungeonUtils.currentDungeonPlayer
    if (DungeonMap.playerHead) {
        mc.player?.skin?.let { skin ->
            fill(-5, -5, 5, 5, DungeonUtils.currentDungeonPlayer.clazz.color.rgba)
            PlayerFaceExtractor.extractRenderState(this, skin, -4, -4, 8)
        }
    } else blit(RenderPipelines.GUI_TEXTURED, marker, -2, -3, 2f, 0f, 5, 7, 8, 8)
    pose().popMatrix()
}

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
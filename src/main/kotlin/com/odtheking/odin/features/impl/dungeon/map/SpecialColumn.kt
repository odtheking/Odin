package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MapUpdateEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.map.tile.DungeonRoom
import com.odtheking.odin.features.impl.dungeon.map.tile.RoomType
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object SpecialColumn {
    val openedSpecialRooms = mutableSetOf<DungeonRoom>()
    var discoveredFullSpecialColumn = 0
    var columnRoomCount = 0
    var discovered1x1s = 0
    var column = -1

    init {
        on<LevelEvent.Load> { unload() }
        on<RoomEnterEvent> { room?.let { update() } }
        on<MapUpdateEvent> { update() }
    }

    fun unload() {
        column = -1
        discoveredFullSpecialColumn = 0
        discovered1x1s = 0
        openedSpecialRooms.clear()
        columnRoomCount = 0
    }

    fun update() {
        val size = if (DungeonScan.startX == 5) 6 else 5

        for (tile in DungeonScan.pathHints) {
            if (tile.position.x == column) {
                if (tile.room?.isKnown1x1 == false) {
                    tile.room?.isKnown1x1 = true
                    discovered1x1s++
                    columnRoomCount++
                }
            } else if (isVisibleFromAllSides(tile.position.x, tile.position.z, size)) {
                if (tile.room?.isKnown1x1 == false) {
                    tile.room?.isKnown1x1 = true
                    discovered1x1s++
                }
            }
        }
        DungeonScan.rooms.filter { it.type.equalsOneOf(RoomType.CHAMPION, RoomType.TRAP, RoomType.PUZZLE) && it.isViewable }.forEach { openedSpecialRooms.add(it) }

        discoveredFullSpecialColumn =
            if (discovered1x1s == DungeonUtils.puzzleCount + 2) size
            else if (column != -1) countDiscoveredInColumn(size) else 0
    }

    private fun checkSide(nx: Int, nz: Int, gridSize: Int): Boolean {
        if (nx !in 0 until gridSize || nz !in 0 until 6) return true

        val room = DungeonScan.tiles.getOrNull(nx + nz * 6)?.room ?: return false

        return room.isViewable || DungeonScan.pathHints.any { it.position.x == nx && it.position.z == nz }
    }

    private fun isVisibleFromAllSides(x: Int, z: Int, gridSize: Int): Boolean {
        val right = checkSide(x + 1, z, gridSize)
        val left = checkSide(x - 1, z, gridSize)
        val bottom = checkSide(x, z + 1, gridSize)
        val top = checkSide(x, z - 1, gridSize)

        val allKnown = right && left && bottom && top

        return allKnown
    }

    private fun countDiscoveredInColumn(zSize: Int): Int {
        var discovered = zSize
        for (z in 0 until zSize) {
            val tile = DungeonScan.tiles[(column - 1) + z * 6]
            val room = tile.room
            if (room == null || (room.isViewable && room.type != RoomType.BLOOD) || room.isViewable)
                discovered--
        }
        return discovered
    }

    fun colorGuessForUnknown(tileX: Int): Array<Color> {
        val specialColumnRoomCount = if (column != -1 && columnRoomCount == 0) 1 else columnRoomCount
        val z = if (DungeonScan.startX == 5) 6 else 5

        return if (tileX == column) guessSpecialTileColor(specialColumnRoomCount, z)
        else guessNonSpecialTileColor(specialColumnRoomCount)
    }

    private fun guessSpecialTileColor(specialColumnRoomCount: Int, mapSizeZ: Int): Array<Color> {
        val specialSize = mapSizeZ - discoveredFullSpecialColumn + specialColumnRoomCount

        return when {
            specialSize <= DungeonUtils.puzzleCount || discovered1x1s - specialColumnRoomCount >= 2 ->
                arrayOf(DungeonMap.puzzleRoomColor)

            openedSpecialRooms.any { it.type == RoomType.TRAP && it.topLeft.x != column } ->
                arrayOf(DungeonMap.puzzleRoomColor)

            specialSize == DungeonUtils.puzzleCount + 1 ->
                arrayOf(DungeonMap.trapRoomColor, DungeonMap.puzzleRoomColor)

            discovered1x1s - specialColumnRoomCount == 1 ->
                arrayOf(DungeonMap.trapRoomColor, DungeonMap.puzzleRoomColor)

            else -> arrayOf(DungeonMap.championRoomColor, DungeonMap.trapRoomColor, DungeonMap.puzzleRoomColor)
        }
    }

    private fun guessNonSpecialTileColor(specialColumnRoomCount: Int): Array<Color> {
        val nonSpecialPuzzles = openedSpecialRooms.count { it.type == RoomType.PUZZLE && it.topLeft.x != column }
        val totalPuzzles = specialColumnRoomCount + nonSpecialPuzzles

        if (totalPuzzles == DungeonUtils.puzzleCount) {
            return when {
                openedSpecialRooms.any { it.type == RoomType.TRAP } ->
                    arrayOf(DungeonMap.championRoomColor)
                openedSpecialRooms.any { it.type == RoomType.CHAMPION } ->
                    arrayOf(DungeonMap.trapRoomColor)
                else -> arrayOf(DungeonMap.championRoomColor, DungeonMap.trapRoomColor)
            }
        }

        if (specialColumnRoomCount == DungeonUtils.puzzleCount + 1)
            return arrayOf(DungeonMap.championRoomColor)

        if (openedSpecialRooms.count { it.type in setOf(RoomType.CHAMPION, RoomType.TRAP) } == 2)
            return arrayOf(DungeonMap.puzzleRoomColor)

        if (openedSpecialRooms.size == DungeonUtils.puzzleCount + 1) {
            return when {
                openedSpecialRooms.none { it.type == RoomType.TRAP } -> arrayOf(DungeonMap.trapRoomColor)
                openedSpecialRooms.none { it.type == RoomType.CHAMPION } -> arrayOf(DungeonMap.championRoomColor)
                else -> arrayOf(DungeonMap.puzzleRoomColor)
            }
        }

        val result = mutableListOf(DungeonMap.puzzleRoomColor)
        if (openedSpecialRooms.none { it.type == RoomType.TRAP }) result.add(DungeonMap.trapRoomColor)
        if (openedSpecialRooms.none { it.type == RoomType.CHAMPION }) result.add(DungeonMap.championRoomColor)

        return result.toTypedArray()
    }
}
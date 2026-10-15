package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MapUpdateEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.map.tile.DungeonRoom
import com.odtheking.odin.features.impl.dungeon.map.tile.RoomShape
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

        for ((position, room) in DungeonScan.tiles) {
            if (room == null || room.isKnown1x1 || room.isViewable) continue
            if (room.data?.shape.let { it != null && it != RoomShape.OneByOne }) continue
            if (!touchesViewableRoom(position.x, position.z, room)) continue

            if (position.x == column) {
                room.isKnown1x1 = true
                discovered1x1s++
                columnRoomCount++
            } else if (isVisibleFromAllSides(position.x, position.z, size, room)) {
                room.isKnown1x1 = true
                discovered1x1s++
            }
        }
        DungeonScan.rooms.filter { it.type.equalsOneOf(RoomType.CHAMPION, RoomType.TRAP, RoomType.PUZZLE) && it.isViewable }.forEach { openedSpecialRooms.add(it) }

        discoveredFullSpecialColumn =
            if (discovered1x1s == DungeonUtils.puzzleCount + 2) size
            else if (column != -1) countDiscoveredInColumn(size) else 0
    }

    private fun touchesViewableRoom(x: Int, z: Int, self: DungeonRoom): Boolean =
        DungeonScan.directions.any { (dx, dz) ->
            val nx = x + dx
            val nz = z + dz
            nx in 0..5 && nz in 0..5 && DungeonScan.tiles[nx + nz * 6].room?.let { it !== self && it.isViewable } == true
        }

    private fun isPathHint(x: Int, z: Int) = DungeonScan.pathHints.any { it.position.x == x && it.position.z == z }

    private fun checkSide(nx: Int, nz: Int, gridSize: Int, self: DungeonRoom, selfIsHint: Boolean): Boolean {
        if (nx !in 0 until gridSize || nz !in 0 until 6) return true

        val room = DungeonScan.tiles.getOrNull(nx + nz * 6)?.room ?: return false
        return room !== self && (room.isViewable || (selfIsHint && isPathHint(nx, nz)))
    }

    private fun isVisibleFromAllSides(x: Int, z: Int, gridSize: Int, self: DungeonRoom): Boolean {
        val selfIsHint = isPathHint(x, z)
        val right = checkSide(x + 1, z, gridSize, self, selfIsHint)
        val left = checkSide(x - 1, z, gridSize, self, selfIsHint)
        val bottom = checkSide(x, z + 1, gridSize, self, selfIsHint)
        val top = checkSide(x, z - 1, gridSize, self, selfIsHint)

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

    private val championPossible get() = openedSpecialRooms.none { it.type == RoomType.CHAMPION }
    private val trapPossible get() = openedSpecialRooms.none { it.type == RoomType.TRAP }
    private val puzzlePossible get() = openedSpecialRooms.count { it.type == RoomType.PUZZLE } < DungeonUtils.puzzleCount

    private fun possible(vararg guesses: Color): Array<Color> {
        val allowed = { color: Color ->
            when (color) {
                DungeonMap.championRoomColor -> championPossible
                DungeonMap.trapRoomColor -> trapPossible
                else -> puzzlePossible
            }
        }
        return guesses.filter(allowed).ifEmpty { listOf(DungeonMap.championRoomColor, DungeonMap.trapRoomColor, DungeonMap.puzzleRoomColor).filter(allowed) }
            .ifEmpty { listOf(DungeonMap.puzzleRoomColor) }.toTypedArray()
    }

    private fun guessSpecialTileColor(specialColumnRoomCount: Int, mapSizeZ: Int): Array<Color> {
        val specialSize = mapSizeZ - discoveredFullSpecialColumn + specialColumnRoomCount

        return when {
            specialSize <= DungeonUtils.puzzleCount || discovered1x1s - specialColumnRoomCount >= 2 ->
                possible(DungeonMap.puzzleRoomColor)

            openedSpecialRooms.any { it.type == RoomType.TRAP && it.topLeft.x != column } ->
                possible(DungeonMap.puzzleRoomColor)

            specialSize == DungeonUtils.puzzleCount + 1 || discovered1x1s - specialColumnRoomCount == 1 ->
                possible(DungeonMap.trapRoomColor, DungeonMap.puzzleRoomColor)

            else -> possible(DungeonMap.championRoomColor, DungeonMap.trapRoomColor, DungeonMap.puzzleRoomColor)
        }
    }

    private fun guessNonSpecialTileColor(specialColumnRoomCount: Int): Array<Color> {
        val nonSpecialPuzzles = openedSpecialRooms.count { it.type == RoomType.PUZZLE && it.topLeft.x != column }

        return when {
            specialColumnRoomCount + nonSpecialPuzzles == DungeonUtils.puzzleCount ->
                possible(DungeonMap.championRoomColor, DungeonMap.trapRoomColor)

            specialColumnRoomCount == DungeonUtils.puzzleCount + 1 ->
                possible(DungeonMap.championRoomColor)

            else -> possible(DungeonMap.puzzleRoomColor, DungeonMap.trapRoomColor, DungeonMap.championRoomColor)
        }
    }
}

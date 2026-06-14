package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.features.impl.dungeon.DungeonMap
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomState
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomType

object SpecialColumn {
    var column = -1
    var discoveredFullSpecialColumn = 0
    var columnRoomCount = 0
    var discovered1x1s = 0
    val opened1x1s = mutableSetOf<MapRoom>()

    fun unload() {
        column = -1
        discoveredFullSpecialColumn = 0
        discovered1x1s = 0
        opened1x1s.clear()
        columnRoomCount = 0
    }

    fun updateSpecialColumn() {
        val size = DungMap.mapSize ?: return

        for (x in 0 until size.x) {
            for (z in 0 until size.z) {
                val mapRoom = (MapScanner.list[Vec2i(x, z).roomListIndex()] as? MapRoom.Tile)?.owner ?: continue
                if (mapRoom.tiles.size != 1 || mapRoom.data?.type in setOf(RoomType.BLOOD, RoomType.ENTRANCE)) continue

                when (mapRoom.state) {
                    RoomState.UNOPENED if mapRoom.data?.type != RoomType.BLOOD -> {
                        if (x == column) {
                            if (!mapRoom.isKnown1x1) {
                                mapRoom.isKnown1x1 = true
                                discovered1x1s++
                                columnRoomCount++
                            }
                        } else if (isRoomVisibleFromAllSides(x, z, size)) {
                            if (!mapRoom.isKnown1x1) {
                                mapRoom.isKnown1x1 = true
                                discovered1x1s++
                            }
                        }
                    }
                    !in setOf(RoomState.UNDISCOVERED, RoomState.UNOPENED) if mapRoom.data?.type in setOf(RoomType.CHAMPION, RoomType.TRAP, RoomType.PUZZLE) -> {
                        opened1x1s.add(mapRoom)
                    }

                    else -> {}
                }
            }
        }

        discoveredFullSpecialColumn = if (discovered1x1s == DungeonUtils.puzzleCount + 2) size.z
        else if (column != -1) countDiscoveredInColumn(size.z) else 0
    }

    private fun isRoomVisibleFromAllSides(x: Int, z: Int, size: Vec2i): Boolean {
        fun checkRoom(offsetX: Int, offsetZ: Int): Boolean {
            val tile = MapScanner.list[Vec2i(offsetX, offsetZ).roomListIndex()] as? MapRoom.Tile ?: return false
            val owner = tile.owner ?: return false
            return owner.state != RoomState.UNDISCOVERED &&
                   (owner.state != RoomState.UNOPENED || Vec2i(offsetX, offsetZ) == owner.entryTile)
        }

        if (x != size.x - 1 && x + 1 != column && !checkRoom(x + 1, z)) return false
        if (x != 0 && !checkRoom(x - 1, z)) return false
        if (z != size.z - 1 && !checkRoom(x, z + 1)) return false
        if (z != 0 && !checkRoom(x, z - 1)) return false

        return true
    }

    private fun countDiscoveredInColumn(zSize: Int): Int {
        var discovered = zSize
        for (z in 0 until zSize) {
            val tile = MapScanner.list[Vec2i(column - 1, z).roomListIndex()] as? MapRoom.Tile
            if (tile == null ||
                (tile.owner?.state == RoomState.UNOPENED && tile.owner.data?.type != RoomType.BLOOD) ||
                tile.owner?.state == RoomState.UNDISCOVERED) {
                discovered--
            }
        }
        return discovered
    }

    fun roomColorGuess(mapRoom: MapRoom): Array<Color> {
        val specialColumnRoomCount = if (column != -1 && columnRoomCount == 0) 1 else columnRoomCount
        val mapSizeZ = DungMap.mapSize?.z ?: 6

        if (mapRoom.specialTile) return guessSpecialTileColor(specialColumnRoomCount, mapSizeZ)

        return guessNonSpecialTileColor(specialColumnRoomCount)
    }

    private fun guessSpecialTileColor(specialColumnRoomCount: Int, mapSizeZ: Int): Array<Color> {
        val specialSize = mapSizeZ - discoveredFullSpecialColumn + specialColumnRoomCount

        return when {
            specialSize <= DungeonUtils.puzzleCount || discovered1x1s - specialColumnRoomCount >= 2 ->
                arrayOf(DungeonMap.puzzleRoomColor.darker(DungeonMap.darkenMultiplier))

            opened1x1s.any { it.data?.type == RoomType.TRAP && !it.specialTile } ->
                arrayOf(DungeonMap.puzzleRoomColor.darker(DungeonMap.darkenMultiplier))

            specialSize == DungeonUtils.puzzleCount + 1 ->
                arrayOf(
                    DungeonMap.trapRoomColor.darker(DungeonMap.darkenMultiplier),
                    DungeonMap.puzzleRoomColor.darker(DungeonMap.darkenMultiplier)
                )

            discovered1x1s - specialColumnRoomCount == 1 ->
                arrayOf(
                    DungeonMap.trapRoomColor.darker(DungeonMap.darkenMultiplier),
                    DungeonMap.puzzleRoomColor.darker(DungeonMap.darkenMultiplier)
                )

            else -> arrayOf(
                DungeonMap.championRoomColor.darker(DungeonMap.darkenMultiplier),
                DungeonMap.trapRoomColor.darker(DungeonMap.darkenMultiplier),
                DungeonMap.puzzleRoomColor.darker(DungeonMap.darkenMultiplier)
            )
        }
    }

    private fun guessNonSpecialTileColor(specialColumnRoomCount: Int): Array<Color> {
        val nonspecialPuzzles = opened1x1s.count { it.data?.type == RoomType.PUZZLE && !it.specialTile }
        val totalPuzzles = specialColumnRoomCount + nonspecialPuzzles

        if (totalPuzzles == DungeonUtils.puzzleCount) {
            return when {
                opened1x1s.any { it.data?.type == RoomType.TRAP } ->
                    arrayOf(DungeonMap.championRoomColor.darker(DungeonMap.darkenMultiplier))

                opened1x1s.any { it.data?.type == RoomType.CHAMPION } ->
                    arrayOf(DungeonMap.trapRoomColor.darker(DungeonMap.darkenMultiplier))

                else -> arrayOf(
                    DungeonMap.championRoomColor.darker(DungeonMap.darkenMultiplier),
                    DungeonMap.trapRoomColor.darker(DungeonMap.darkenMultiplier)
                )
            }
        }

        if (specialColumnRoomCount == DungeonUtils.puzzleCount + 1)
            return arrayOf(DungeonMap.championRoomColor.darker(DungeonMap.darkenMultiplier))

        if (opened1x1s.count { it.data?.type in setOf(RoomType.CHAMPION, RoomType.TRAP) } == 2)
            return arrayOf(DungeonMap.puzzleRoomColor.darker(DungeonMap.darkenMultiplier))

        if (opened1x1s.size == DungeonUtils.puzzleCount + 1) {
            return when {
                opened1x1s.none { it.data?.type == RoomType.TRAP } ->
                    arrayOf(DungeonMap.trapRoomColor.darker(DungeonMap.darkenMultiplier))

                opened1x1s.none { it.data?.type == RoomType.CHAMPION } ->
                    arrayOf(DungeonMap.championRoomColor.darker(DungeonMap.darkenMultiplier))

                else -> arrayOf(DungeonMap.puzzleRoomColor.darker(DungeonMap.darkenMultiplier))
            }
        }

        val result = mutableListOf(DungeonMap.puzzleRoomColor.darker(DungeonMap.darkenMultiplier))
        if (opened1x1s.none { it.data?.type == RoomType.TRAP })
            result.add(DungeonMap.trapRoomColor.darker(DungeonMap.darkenMultiplier))

        if (opened1x1s.none { it.data?.type == RoomType.CHAMPION })
            result.add(DungeonMap.championRoomColor.darker(DungeonMap.darkenMultiplier))

        return result.toTypedArray()
    }
}

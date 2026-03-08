package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.features.impl.dungeon.DungeonMap
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.skyblock.dungeon.tiles.*

class MapRoom(val data: RoomData, val height: Int) {
    val tiles = mutableListOf<Tile>()
    val places = mutableListOf<Vec2i>()
    var state = RoomState.UNDISCOVERED
    var rotation: Rotations = Rotations.NONE

    var entryTile: Vec2i? = null
    var isKnown1x1 = false
    var specialTile = false
    var rushRoom = false

    data class StateUpdated(val mapRoom: MapRoom, val old: RoomState, val new: RoomState)

    class RoomTile(val owner: MapRoom, pos: Vec2i) : Tile(pos) {
        override fun size() = Vec2i(16, 16)

        override fun placement(): Vec2i {
            val x = (pos.x + 185) shr 5
            val z = (pos.z + 185) shr 5
            return Vec2i(x * 20, z * 20)
        }

        override fun color(): Array<Color> = when (owner.state) {
            RoomState.UNOPENED if owner.isKnown1x1 -> SpecialColumn.roomColorGuess(owner)
            RoomState.UNOPENED -> {
                if (owner.data.type == RoomType.BLOOD) arrayOf(DungeonMap.bloodRoomColor.darker(DungeonMap.darkenMultiplier))
                else arrayOf(DungeonMap.unopenedRoomColor)
            }
            in setOf(RoomState.UNDISCOVERED, RoomState.UNOPENED) -> arrayOf(getRoomBaseColor().darker(DungeonMap.darkenMultiplier))
            else -> arrayOf(getRoomBaseColor())
        }

        private fun getRoomBaseColor() = when (owner.data.type) {
            RoomType.BLOOD -> DungeonMap.bloodRoomColor
            RoomType.NORMAL -> DungeonMap.normalRoomColor
            RoomType.PUZZLE -> DungeonMap.puzzleRoomColor
            RoomType.CHAMPION -> DungeonMap.championRoomColor
            RoomType.TRAP -> DungeonMap.trapRoomColor
            RoomType.ENTRANCE -> DungeonMap.entranceRoomColor
            RoomType.FAIRY -> DungeonMap.fairyRoomColor
            RoomType.RARE -> DungeonMap.rareRoomColor
        }
    }

    class SepTile(val owner: MapRoom, pos: Vec2i) : Tile(pos) {
        override fun size() = Vec2i(16, 16)

        override fun placement(): Vec2i {
            val x = (pos.x + 185) shr 4
            val z = (pos.z + 185) shr 4
            val xOffset = (x shr 1) * 20 + x % 2 * 4
            val yOffset = (z shr 1) * 20 + z % 2 * 4
            return Vec2i(xOffset, yOffset)
        }

        override fun color(): Array<Color> {
            return if (owner.state in setOf(RoomState.UNDISCOVERED, RoomState.UNOPENED)) arrayOf(DungeonMap.normalRoomColor.darker(DungeonMap.darkenMultiplier))
            else arrayOf(DungeonMap.normalRoomColor)
        }
    }

    fun updateState(placement: Vec2i, color: Int): StateUpdated? {
        if (state == RoomState.GREEN && data.name == "Golden Oasis") return null

        val oldState = state
        state = when (color) {
            0 -> RoomState.UNDISCOVERED
            34 -> RoomState.CLEARED
            18 -> when (data.type) {
                RoomType.BLOOD -> {
                    MapScanner.blood = this
                    RoomState.DISCOVERED
                }
                RoomType.PUZZLE -> RoomState.FAILED
                else -> state
            }
            30 -> when (data.type) {
                RoomType.ENTRANCE -> RoomState.DISCOVERED
                else -> RoomState.GREEN
            }
            85, 119 -> {
                entryTile = placement
                specialTile = placement.x == SpecialColumn.column
                RoomState.UNOPENED
            }
            else -> RoomState.DISCOVERED
        }

        return if (state != oldState) StateUpdated(this, oldState, state) else null
    }

    fun roomTile(pos: Vec2i): RoomTile? {
        if (tiles.any { pos == it.pos }) return null
        return RoomTile(this, pos).also {
            tiles.add(it)
            places.add(pos.add(Vec2i(185, 185)).divide(32))
        }
    }

    fun separator(pos: Vec2i): SepTile? {
        if (tiles.any { pos == it.pos }) return null
        return SepTile(this, pos).also { tiles.add(it) }
    }

    fun textPlacement(): Vec2i {
        if (rotation == Rotations.NONE) return tiles.minBy { it.pos.x * 1000 + it.pos.z }.placement()
        val placements = tiles.map { it.placement() }

        val x = (placements.minOf { it.x } + placements.maxOf { it.x }) / 2
        val z = when (data.shape) {
            RoomShape.L ->
                if (rotation == Rotations.WEST || rotation == Rotations.NORTH) placements.minOf { it.z } else placements.maxOf { it.z }

            else ->
                (placements.minOf { it.z } + placements.maxOf { it.z }) / 2
        }
        return Vec2i(x, z)
    }
}


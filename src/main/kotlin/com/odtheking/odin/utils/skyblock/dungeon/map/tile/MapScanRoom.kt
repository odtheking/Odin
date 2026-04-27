package com.odtheking.odin.utils.skyblock.dungeon.map.tile

import com.odtheking.odin.utils.IVec2

data class MapScanTile(
    override val position: IVec2,
    var room: MapScanRoom? = null
) : ScanTile

enum class MapCheckmark {
    NONE,
    WHITE,
    GREEN,
    RED,
    QUESTION_MARK,
    UNDISCOVERED;

    companion object {
        fun fromMapColor(color: Byte): MapCheckmark? {
            return when (color.toInt()) {
                34 -> WHITE
                30 -> GREEN
                18 -> RED
                119 -> QUESTION_MARK
                else -> null
            }
        }
    }
}

class MapScanRoom(
    override var type: RoomType,
    override var position: IVec2
) : RoomInfo {

    var checkmark: MapCheckmark = MapCheckmark.UNDISCOVERED
    override val segments: ArrayList<ScanTile> = ArrayList(4)
    override var shape: RoomShape = RoomShape.OneByOne
    override var rotation: RoomRotation? = null

    fun addSegment(tile: MapScanTile) {
        if (!segments.contains(tile)) {
            segments.add(tile)
            position = IVec2(segments.minOf { it.position.x }, segments.minOf { it.position.z })
        }
    }

    fun inferLayout(): Pair<RoomShape, RoomRotation?> {
        val positions = segments.map { it.position }
        val minX = positions.minOf { it.x }
        val minZ = positions.minOf { it.z }
        val maxX = positions.maxOf { it.x }
        val maxZ = positions.maxOf { it.z }

        shape = when (positions.size) {
            1    -> RoomShape.OneByOne
            2    -> RoomShape.TwoByOne
            3    -> if ((maxX - minX) == 1 && (maxZ - minZ) == 1) RoomShape.L else RoomShape.ThreeByOne
            4    -> if ((maxX - minX) == 1 && (maxZ - minZ) == 1) RoomShape.TwoByTwo else RoomShape.FourByOne
            else -> RoomShape.OneByOne
        }

        val horizontal = (maxX - minX) > (maxZ - minZ)
        rotation = when (shape) {
            RoomShape.OneByOne -> null
            RoomShape.TwoByOne, RoomShape.ThreeByOne, RoomShape.FourByOne ->
                if (horizontal) RoomRotation.SOUTH else RoomRotation.WEST
            RoomShape.L -> {
                val set = positions.toHashSet()
                when {
                    IVec2(maxX, minZ) !in set -> RoomRotation.WEST
                    IVec2(minX, maxZ) !in set -> RoomRotation.NORTH
                    IVec2(minX, minZ) !in set -> RoomRotation.EAST
                    else                       -> RoomRotation.SOUTH
                }
            }
            RoomShape.TwoByTwo -> RoomRotation.SOUTH
        }

        return shape to rotation
    }
}
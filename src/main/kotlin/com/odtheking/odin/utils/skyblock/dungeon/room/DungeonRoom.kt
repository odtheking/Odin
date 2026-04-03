package com.odtheking.odin.utils.skyblock.dungeon.room

import com.odtheking.odin.utils.IVec2

data class DungeonTile(
    val position: IVec2,
    var room: DungeonRoom? = null
)

enum class MapCheckmark(val symbol: String) {
    NONE("§0N"),
    WHITE("W"),
    GREEN("§aG"),
    RED("§cR"),
    QUESTION_MARK("§d?"),
    UNDISCOVERED("§8X");

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

// everything is super schizo ngl, but I can't be asked to fix it

private fun ArrayList<DungeonTile>.minCorner() = IVec2(minOf { it.position.x }, minOf { it.position.z })

sealed class DungeonRoom {

    val segments: ArrayList<DungeonTile> = ArrayList(4)
    var checkmark: MapCheckmark = MapCheckmark.UNDISCOVERED

    class Collecting : DungeonRoom()

    /** All tiles resolved from the map item. rotation is null for 1×1 — map geometry can't determine it. */
    class MapResolved(
        val shape: RoomShape,
        val rotation: RoomRotation?,
        val type: RoomType,
    ) : DungeonRoom() {
        val position: IVec2 by lazy { segments.minCorner() }
    }

    /** All tiles confirmed by world scan. rotation always non-null (block markers for 1×1). */
    class WorldResolved(
        val worldData: RoomData,
        val rotation: RoomRotation,
    ) : DungeonRoom() {
        val position: IVec2 by lazy { segments.minCorner() }
        val shape: RoomShape = worldData.shape
        val type: RoomType   = worldData.type
    }

    companion object {
        fun inferLayout(positions: List<IVec2>): Pair<RoomShape, RoomRotation?> {
            val minX = positions.minOf { it.x }
            val minZ = positions.minOf { it.z }
            val maxX = positions.maxOf { it.x }
            val maxZ = positions.maxOf { it.z }
            val horizontal = (maxX - minX) > (maxZ - minZ)

            return when (positions.size) {
                1 -> RoomShape.OneByOne to null
                2 -> RoomShape.TwoByOne to if (horizontal) RoomRotation.SOUTH else RoomRotation.NORTH
                3 -> if ((maxX - minX) == 1 && (maxZ - minZ) == 1) {
                    val set = positions.toHashSet()
                    RoomShape.L to when {
                        IVec2(maxX, minZ) !in set -> RoomRotation.WEST   // top-right missing
                        IVec2(minX, maxZ) !in set -> RoomRotation.NORTH  // bottom-left missing
                        IVec2(minX, minZ) !in set -> RoomRotation.EAST   // top-left missing
                        else                       -> RoomRotation.SOUTH  // bottom-right missing
                    }
                } else RoomShape.ThreeByOne to if (horizontal) RoomRotation.SOUTH else RoomRotation.NORTH
                4 ->
                    if ((maxX - minX) == 1 && (maxZ - minZ) == 1) RoomShape.TwoByTwo to RoomRotation.SOUTH
                    else RoomShape.FourByOne to if (horizontal) RoomRotation.SOUTH else RoomRotation.NORTH

                else -> RoomShape.OneByOne to RoomRotation.SOUTH
            }
        }
    }
}
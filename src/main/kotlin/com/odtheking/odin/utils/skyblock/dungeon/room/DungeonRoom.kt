package com.odtheking.odin.utils.skyblock.dungeon.room

import com.odtheking.odin.utils.IVec2

class DungeonTile(val position: IVec2, var room: DungeonRoom? = null)

enum class MapCheckmark(val mapColor: Byte, val symbol: String) {
    NONE(-1, "§0N"),
    WHITE(34, "W"),
    GREEN(30, "§aG"),
    RED(18, "§cR"),
    QUESTION_MARK(119, "§d?"),
    UNDISCOVERED(-1, "§8X");

    companion object {
        private val BY_COLOR = entries.associateBy { it.mapColor }
        fun fromMapColor(color: Byte) = BY_COLOR[color]
    }
}

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
        val position: IVec2 = segments.minCorner()
    }

    /** All tiles confirmed by world scan. rotation always non-null (block markers for 1×1). */
    class WorldResolved(
        val worldData: RoomData,
        val rotation: RoomRotation,
    ) : DungeonRoom() {
        val position: IVec2 = segments.minCorner()
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
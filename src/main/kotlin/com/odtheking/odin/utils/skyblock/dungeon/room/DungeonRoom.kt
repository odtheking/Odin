package com.odtheking.odin.utils.skyblock.dungeon.room

import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.skyblock.dungeon.DungeonScan
import com.odtheking.odin.utils.skyblock.dungeon.DungeonScan.getBlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.LevelChunk
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DungeonTile(
    val position: IVec2,
    var room: DungeonRoom? = null
)

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

class DungeonRoom(
    var data: RoomData? = null,
    val segments: ArrayList<DungeonTile> = ArrayList(4),

    var shape: RoomShape = RoomShape.UNKNOWN,
    var rotation: RoomRotation = RoomRotation.SOUTH,
    var position: IVec2 = IVec2(0, 0),

    var checkmark: MapCheckmark = MapCheckmark.UNDISCOVERED,
    var type: RoomType = RoomType.UNDISCOVERED,
) {

    init {
        DungeonScan.rooms.add(this)
    }

    fun hasAllSegments(): Boolean {
        return data?.shape?.segments == segments.size
    }

    fun setShapeAndRotation(chunk: LevelChunk, chunkPosition: IVec2, highestBlock: Int) {
        // for future reference to myself to prevent misusing it
        assert(hasAllSegments()) { "Called getShapeFromSegments without having enough segments, Or on a room without data." }
        assert(shape === RoomShape.UNKNOWN) { "Called getShapeFromSegments on a complete room." }

        val segmentAmount = segments.size
        assert(segmentAmount in 1..4) { "Invalid amount of segments for this room." }

        var minX = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxZ = Int.MIN_VALUE

        for (segment in segments) {
            minX = min(minX, segment.position.x)
            minZ = min(minZ, segment.position.z)
            maxX = max(maxX, segment.position.x)
            maxZ = max(maxZ, segment.position.z)
        }

        this.position = IVec2(minX, minZ)
        val length = (maxX - minX) + 1
        val height = (maxZ - minZ) + 1

        when (segmentAmount) {
            1 -> {
                val pos = chunkPosition * 16
                shape = RoomShape.OneByOne
                rotation = when {
                    chunk.getBlockState(pos.x + 14, highestBlock, pos.z).block === Blocks.BLUE_TERRACOTTA -> RoomRotation.WEST
                    chunk.getBlockState(pos.x + 14, highestBlock, pos.z + 14).block === Blocks.BLUE_TERRACOTTA -> RoomRotation.NORTH
                    chunk.getBlockState(pos.x, highestBlock, pos.z + 14).block === Blocks.BLUE_TERRACOTTA -> RoomRotation.EAST
                    else -> RoomRotation.SOUTH
                }
            }
            2 -> {
                shape = RoomShape.TwoByOne
                rotation = if (length == 1) RoomRotation.WEST else RoomRotation.SOUTH
            }
            3 -> {
                if (length == 2 && height == 2) {
                    shape = RoomShape.L

                    val corner = segments.first { a ->
                        segments.all { b ->
                            abs(a.position.x - b.position.x) + abs(a.position.z - b.position.z) <= 1
                        }
                    }.position

                    rotation = when (corner) {
                        IVec2(minX, minZ) -> RoomRotation.SOUTH
                        IVec2(maxX, minZ) -> RoomRotation.NORTH
                        IVec2(maxX, maxZ) -> RoomRotation.EAST
                        else -> RoomRotation.WEST
                    }
                } else {
                    shape = RoomShape.ThreeByOne
                    rotation = if (length == 1) RoomRotation.WEST else RoomRotation.SOUTH
                }
            }
            4 -> {
                if (length == 2 && height == 2) {
                    shape = RoomShape.TwoByTwo
                    rotation = RoomRotation.SOUTH
                } else {
                    shape = RoomShape.FourByOne
                    rotation = if (length == 1) RoomRotation.WEST else RoomRotation.SOUTH
                }
            }
        }
    }
}
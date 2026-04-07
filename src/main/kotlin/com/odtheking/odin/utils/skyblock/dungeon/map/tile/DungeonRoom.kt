package com.odtheking.odin.utils.skyblock.dungeon.map.tile

import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.IVec2
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

data class DungeonTile(
    override val position: IVec2,
    var room: DungeonRoom? = null
) : ScanTile

class DungeonRoom(val data: RoomData, initialPosition: IVec2) : RoomInfo {
    override val segments: ArrayList<ScanTile> = ArrayList(4)

    var clayPos: BlockPos? = null
    var waypoints: MutableSet<DungeonWaypoints.DungeonWaypoint> = mutableSetOf()

    override var position: IVec2 = initialPosition
    override var rotation: RoomRotation? = null
    override val shape: RoomShape get() = data.shape
    override val type: RoomType get() = data.type

    val name: String get() = data.name

    fun addSegment(segment: DungeonTile) {
        if (!segments.contains(segment)) {
            segments.add(segment)
            position = IVec2(segments.minOf { it.position.x }, segments.minOf { it.position.z })
        }
    }

    fun inferLayout(getBlock: (BlockPos) -> Block, highestBlock: Int) {
        val positions = segments.map { it.position }

        val rotationsToTry = if (shape != RoomShape.OneByOne) {
            val minX = positions.minOf { it.x }
            val minZ = positions.minOf { it.z }
            val maxX = positions.maxOf { it.x }
            val maxZ = positions.maxOf { it.z }
            val horizontal = (maxX - minX) > (maxZ - minZ)
            val geometryRotation = when (shape) {
                RoomShape.TwoByOne, RoomShape.ThreeByOne, RoomShape.FourByOne ->
                    if (horizontal) RoomRotation.SOUTH else RoomRotation.NORTH
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
                else               -> RoomRotation.SOUTH
            }
            rotation = geometryRotation
            listOf(geometryRotation)
        } else RoomRotation.entries

        for (r in rotationsToTry) {
            for (tile in segments) {
                val cx = tile.position.x * 32 - 185
                val cz = tile.position.z * 32 - 185
                val pos = BlockPos(cx + r.dx, highestBlock, cz + r.dz)
                if (getBlock(pos) === Blocks.BLUE_TERRACOTTA) {
                    clayPos = pos
                    rotation = r
                    return
                }
            }
        }
    }
}
package com.odtheking.odin.utils.skyblock.dungeon.map.tile

import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.IVec2
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

data class DungeonTile(
    override val position: IVec2,
    var room: DungeonRoom? = null
) : ScanTile

class DungeonRoom(val data: RoomData, initialPosition: IVec2) : RoomInfo {
    override val segments: ArrayList<ScanTile> = ArrayList(4)

    var discovered: Boolean = false
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
        if (shape == RoomShape.OneByOne && applyFairyFallback(highestBlock)) return

        val positions = segments.map { it.position }
        val rotationsToTry = if (shape == RoomShape.OneByOne) {
            RoomRotation.entries
        } else {
            listOf(resolveGeometryRotation(positions).also { rotation = it })
        }

        findClayForRotations(rotationsToTry, highestBlock, getBlock) { _, pos ->
            shape != RoomShape.OneByOne || isOneByOneClayCandidate(pos, getBlock)
        }?.let { (foundRotation, foundPos) ->
            rotation = foundRotation
            clayPos = foundPos
        }
    }

    private fun applyFairyFallback(highestBlock: Int): Boolean {
        if (data.name != "Fairy") return false
        val tile = segments.firstOrNull() ?: return true
        clayPos = BlockPos(tile.position.x * 32 - 200, highestBlock, tile.position.z * 32 - 200)
        rotation = RoomRotation.SOUTH
        return true
    }

    private fun resolveGeometryRotation(positions: List<IVec2>): RoomRotation {
        val minX = positions.minOf { it.x }
        val minZ = positions.minOf { it.z }
        val maxX = positions.maxOf { it.x }
        val maxZ = positions.maxOf { it.z }
        val horizontal = (maxX - minX) > (maxZ - minZ)

        return when (shape) {
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
            else               -> RoomRotation.SOUTH
        }
    }

    private fun findClayForRotations(
        rotations: Iterable<RoomRotation>,
        highestBlock: Int,
        getBlock: (BlockPos) -> Block,
        validator: (RoomRotation, BlockPos) -> Boolean,
    ): Pair<RoomRotation, BlockPos>? {
        for (roomRotation in rotations) {
            for (tile in segments) {
                val pos = tile.clayProbePos(roomRotation, highestBlock)
                if (getBlock(pos) !== Blocks.BLUE_TERRACOTTA) continue
                if (validator(roomRotation, pos)) return roomRotation to pos
            }
        }
        return null
    }

    private fun isOneByOneClayCandidate(pos: BlockPos, getBlock: (BlockPos) -> Block): Boolean {
        return Direction.entries.asSequence()
            .filter { it.axis.isHorizontal }
            .all { facing ->
                getBlock(pos.offset(facing.stepX, 0, facing.stepZ)).let { block ->
                    block === Blocks.AIR || block === Blocks.BLUE_TERRACOTTA
                }
            }
    }

    private fun ScanTile.clayProbePos(rotation: RoomRotation, y: Int): BlockPos {
        val cx = position.x * 32 - 185
        val cz = position.z * 32 - 185
        return BlockPos(cx + rotation.dx, y, cz + rotation.dz)
    }
}
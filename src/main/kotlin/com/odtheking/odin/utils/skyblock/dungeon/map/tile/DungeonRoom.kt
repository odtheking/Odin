package com.odtheking.odin.utils.skyblock.dungeon.map.tile

import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.rotateAroundNorth
import com.odtheking.odin.utils.rotateToNorth
import net.minecraft.core.BlockPos
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
        if (applyFairyFallback(highestBlock)) return

        val positions = segments.map { it.position }

        if (shape == RoomShape.OneByOne) {
            findOneByOneClay(getBlock, highestBlock)?.let { (rot, pos) ->
                rotation = rot
                clayPos = pos
            }
            return
        }

        rotation = resolveGeometryRotation(positions)

        val (x, z) = getRealPosition(positions.minOf { it.x }, positions.minOf { it.z })
        clayPos = BlockPos(x - 15, highestBlock, z - 15)
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

    private fun findOneByOneClay(getBlock: (BlockPos) -> Block, highestBlock: Int, ): Pair<RoomRotation, BlockPos>? {
        for (rot in RoomRotation.entries) {
            val pos = clayProbePos(rot, highestBlock)
            if (getBlock(pos) === Blocks.BLUE_TERRACOTTA) return rot to pos
        }
        return null
    }

    private fun applyFairyFallback(highestBlock: Int): Boolean {
        if (data.name != "Fairy") return false
        clayPos = clayProbePos(RoomRotation.SOUTH, highestBlock)
        rotation = RoomRotation.SOUTH
        return true
    }

    private fun clayProbePos(rotation: RoomRotation, y: Int): BlockPos {
        val (x, z) = getRealPosition()
        return BlockPos(x + rotation.dx, y, z + rotation.dz)
    }

    fun getRealPosition() = position.x * 32 - 185 to position.z * 32 - 185
    fun getRealPosition(x: Int, z: Int) = x * 32 - 185 to z * 32 - 185

    fun getRelativeCoords(pos: BlockPos): BlockPos {
        val clay = clayPos ?: return BlockPos.ZERO
        val rot = rotation ?: return BlockPos.ZERO

        return pos.subtract(clay.atY(0)).rotateToNorth(rot)
    }

    fun getRealCoords(pos: BlockPos): BlockPos {
        val clay = clayPos ?: return BlockPos.ZERO
        val rot = rotation ?: return BlockPos.ZERO

        return pos.rotateAroundNorth(rot).offset(clay.x, 0, clay.z)
    }
}
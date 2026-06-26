package com.odtheking.odin.utils.skyblock.dungeon.map.tile

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.rotateAroundNorth
import com.odtheking.odin.utils.rotateToNorth
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks

class DungeonRoom(var type: RoomType, initialPosition: IVec2, var data: RoomData? = null) {
    val segments: ArrayList<IVec2> = ArrayList(4)
    var position: IVec2 = initialPosition
    var rotation: RoomRotation? = null

    var shape: RoomShape = RoomShape.OneByOne

    var discovered: Boolean = false
    var clayPos: BlockPos? = null
    var highestBlock: Int? = null
    var waypoints: MutableSet<DungeonWaypoints.DungeonWaypoint> = mutableSetOf()

    var checkmark: MapCheckmark = MapCheckmark.UNDISCOVERED

    val isViewable: Boolean get() = discovered || checkmark != MapCheckmark.UNDISCOVERED
    val name: String? get() = data?.name

    fun addSegment(segment: DungeonTile) {
        if (!segments.contains(segment.position)) {
            segments.add(segment.position)
            position = IVec2(segments.minOf { it.x }, segments.minOf { it.z })
        }
    }

    fun inferLayoutFromMap() {
        val minX = segments.minOf { it.x }
        val minZ = segments.minOf { it.z }
        val maxX = segments.maxOf { it.x }
        val maxZ = segments.maxOf { it.z }

        shape = when (segments.size) {
            1    -> RoomShape.OneByOne
            2    -> RoomShape.TwoByOne
            3    -> if ((maxX - minX) == 1 && (maxZ - minZ) == 1) RoomShape.L else RoomShape.ThreeByOne
            4    -> if ((maxX - minX) == 1 && (maxZ - minZ) == 1) RoomShape.TwoByTwo else RoomShape.FourByOne
            else -> RoomShape.OneByOne
        }

        resolveGeometryRotation()?.let { (rot, pos) ->
            rotation = rot
            clayPos = pos
        }
    }

    fun inferLayout(highestBlock: Int) {
        this.highestBlock = highestBlock

        val roomData = data ?: return

        if (applyFairyFallback(highestBlock)) return

        shape = roomData.shape

        if (shape == RoomShape.OneByOne) {
            get1x1Rotation()
            return
        }

        resolveGeometryRotation()?.let { (rot, pos) ->
            rotation = rot
            clayPos = pos
        }
    }

    private fun resolveGeometryRotation(): Pair<RoomRotation, BlockPos>? {
        val topLeft     = segments.minByOrNull { it.x * 1000 + it.z } ?: return null
        val bottomRight = segments.maxByOrNull { it.x * 1000 + it.z } ?: return null

        val (tlX, tlZ) = getRealPosition(topLeft.x, topLeft.z)
        val (brX, brZ) = getRealPosition(bottomRight.x, bottomRight.z)

        val height = highestBlock ?: return null

        return if (shape == RoomShape.L) {
            val other = segments.find { it != topLeft && it != bottomRight } ?: return null
            val (otX, otZ) = getRealPosition(other.x, other.z)

            when {
                topLeft.x == bottomRight.x -> RoomRotation.EAST to BlockPos(otX - 15, height, tlZ + 15)
                topLeft.z == bottomRight.z -> RoomRotation.WEST to BlockPos(brX + 15, height, brZ - 15)
                other.x == topLeft.x -> RoomRotation.SOUTH to BlockPos(tlX - 15, height, tlZ - 15)
                else -> RoomRotation.NORTH to BlockPos(brX + 15, height, brZ + 15)
            }
        } else {
            if (topLeft.x == bottomRight.x) RoomRotation.WEST to BlockPos(tlX + 15, height, tlZ - 15)
            else RoomRotation.SOUTH to BlockPos(tlX - 15, height, tlZ - 15)
        }
    }

    fun get1x1Rotation(): Boolean {
        if (shape != RoomShape.OneByOne) return false
        val y = highestBlock ?: return false
        if (applyFairyFallback(y)) return true

        for (rot in RoomRotation.entries) {
            val pos = clayProbePos(rot, y)
            if (mc.level?.getBlockState(pos)?.block == Blocks.BLUE_TERRACOTTA) {
                rotation = rot
                clayPos = pos
                return true
            }
        }
        return false
    }

    private fun applyFairyFallback(highestBlock: Int): Boolean {
        if (data?.name != "Fairy") return false
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
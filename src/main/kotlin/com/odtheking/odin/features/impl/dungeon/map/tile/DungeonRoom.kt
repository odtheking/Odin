package com.odtheking.odin.features.impl.dungeon.map.tile

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.features.impl.dungeon.map.DungeonScan
import com.odtheking.odin.utils.*
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks

class DungeonRoom(var type: RoomType, initialPosition: IVec2, var data: RoomData? = null) {
    val tiles: ArrayList<IVec2> = ArrayList(4)
    var topLeft: IVec2 = initialPosition
    var rotation: RoomRotation? = null
        set(value) {
            field = value
            recalculateCenter()
        }

    var shape: RoomShape = RoomShape.OneByOne

    var walkedInto: Boolean = false
    var clayPos: BlockPos? = null
    var highestBlock: Int? = null
    var waypoints: MutableSet<DungeonWaypoints.DungeonWaypoint> = mutableSetOf()

    var checkmark: MapCheckmark = MapCheckmark.UNDISCOVERED

    var isKnown1x1: Boolean = false

    var center: IVec2? = null
        private set

    val isViewable: Boolean get() = walkedInto || checkmark != MapCheckmark.UNDISCOVERED
    val name: String? get() = data?.name

    fun addSegment(segment: DungeonTile) {
        if (!tiles.contains(segment.position)) {
            tiles.add(segment.position)
            topLeft = IVec2(tiles.minOf { it.x }, tiles.minOf { it.z })
            highestBlock?.let { if (tiles.size == data?.shape?.tileAmount) inferLayout(it) }
            recalculateCenter()
        }
    }

    fun occupiedTiles(): List<IVec2> {
        val rot = rotation ?: return emptyList()
        return tileOffsets(rot).map { topLeft + it }
    }

    private fun tileOffsets(rot: RoomRotation): List<IVec2> = when (shape) {
        RoomShape.OneByOne -> listOf(IVec2(0, 0))
        RoomShape.TwoByTwo -> listOf(IVec2(0, 0), IVec2(1, 0), IVec2(0, 1), IVec2(1, 1))
        RoomShape.L -> when (rot) {
            RoomRotation.WEST  -> listOf(IVec2(0, 0), IVec2(1, 0), IVec2(0, 1))
            RoomRotation.NORTH -> listOf(IVec2(0, 0), IVec2(1, 0), IVec2(1, 1))
            else               -> listOf(IVec2(0, 0), IVec2(0, 1), IVec2(1, 1))
        }
        else -> if (rot == RoomRotation.SOUTH) (0 until shape.tileAmount).map { IVec2(it, 0) }
                else (0 until shape.tileAmount).map { IVec2(0, it) }
    }

    private fun recalculateCenter() {
        fun tileCenter(tile: IVec2) = IVec2(tile.x * DungeonScan.MAP_ROOM_GAP + DungeonScan.MAP_ROOM_SIZE / 2, tile.z * DungeonScan.MAP_ROOM_GAP + DungeonScan.MAP_ROOM_SIZE / 2)

        val rot = rotation
        if (rot == null) {
            if (tiles.isEmpty()) return
            center = tileCenter(tiles.minBy { it.sortKey })
            return
        }

        val corners = occupiedTiles().map(::tileCenter)
        val z = when (shape) {
            RoomShape.L ->
                if (rot == RoomRotation.NORTH || rot == RoomRotation.WEST) corners.minOf { it.z }
                else corners.maxOf { it.z }
            else -> (corners.minOf { it.z } + corners.maxOf { it.z }) / 2
        }
        center = IVec2((corners.minOf { it.x } + corners.maxOf { it.x }) / 2, z)
    }

    fun inferLayoutFromMap() {
        val minX = tiles.minOf { it.x }
        val minZ = tiles.minOf { it.z }
        val maxX = tiles.maxOf { it.x }
        val maxZ = tiles.maxOf { it.z }

        shape = when (tiles.size) {
            1    -> RoomShape.OneByOne
            2    -> RoomShape.TwoByOne
            3    -> if ((maxX - minX) == 1 && (maxZ - minZ) == 1) RoomShape.L else RoomShape.ThreeByOne
            4    -> if ((maxX - minX) == 1 && (maxZ - minZ) == 1) RoomShape.TwoByTwo else RoomShape.FourByOne
            else -> RoomShape.OneByOne
        }

        resolveGeometryRotation()?.let { rotation = it }
    }

    fun inferLayout(highestBlock: Int) {
        val roomData = data ?: return

        if (applyFairyFallback(highestBlock)) return

        shape = roomData.shape

        if (shape == RoomShape.OneByOne) {
            get1x1Rotation()
            return
        }

        val rot = resolveGeometryRotation()
        val pos = rot?.let { resolveClayPos(it, highestBlock) }

        if (rot == null || pos == null) {
            modMessage("Failed to resolve geometry for ${data?.name ?: type} at $topLeft with tiles $tiles")
            return
        }

        rotation = rot
        clayPos = pos
    }

    private fun resolveGeometryRotation(): RoomRotation? {
        val bottomRight = tiles.maxByOrNull { it.sortKey } ?: return null

        return if (shape == RoomShape.L) {
            val other = tiles.find { it != topLeft && it != bottomRight } ?: return null
            when {
                topLeft.x == bottomRight.x -> RoomRotation.EAST
                topLeft.z == bottomRight.z -> RoomRotation.WEST
                other.x == topLeft.x -> RoomRotation.SOUTH
                else -> RoomRotation.NORTH
            }
        } else {
            if (topLeft.x == bottomRight.x) RoomRotation.WEST else RoomRotation.SOUTH
        }
    }

    private fun resolveClayPos(rotation: RoomRotation, height: Int): BlockPos? {
        val bottomRight = tiles.maxByOrNull { it.sortKey } ?: return null

        val (tlX, tlZ) = getRealPosition(topLeft.x, topLeft.z)
        val (brX, brZ) = getRealPosition(bottomRight.x, bottomRight.z)

        return if (shape == RoomShape.L) {
            val other = tiles.find { it != topLeft && it != bottomRight } ?: return null
            val (otX, _) = getRealPosition(other.x, other.z)

            when (rotation) {
                RoomRotation.EAST  -> BlockPos(otX - 15, height, tlZ + 15)
                RoomRotation.WEST  -> BlockPos(brX + 15, height, brZ - 15)
                RoomRotation.SOUTH -> BlockPos(tlX - 15, height, tlZ - 15)
                RoomRotation.NORTH -> BlockPos(brX + 15, height, brZ + 15)
            }
        } else {
            if (rotation == RoomRotation.WEST) BlockPos(tlX + 15, height, tlZ - 15)
            else BlockPos(tlX - 15, height, tlZ - 15)
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

    fun getRealPosition(x: Int = topLeft.x, z: Int = topLeft.z) = x * 32 - 185 to z * 32 - 185

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
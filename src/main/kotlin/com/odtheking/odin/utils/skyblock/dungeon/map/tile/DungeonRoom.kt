package com.odtheking.odin.utils.skyblock.dungeon.map.tile

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.rotateAroundNorth
import com.odtheking.odin.utils.rotateToNorth
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks

data class DungeonTile(
    override val position: IVec2,
    var room: DungeonRoom? = null
) : ScanTile

enum class MapCheckmark {
    NONE, WHITE, GREEN, RED, QUESTION_MARK, UNDISCOVERED;

    companion object {
        fun fromMapColor(color: Byte): MapCheckmark? = when (color.toInt()) {
            34   -> WHITE
            30   -> GREEN
            18   -> RED
            119  -> QUESTION_MARK
            else -> null
        }
    }
}

class DungeonRoom(
    override var type: RoomType,
    initialPosition: IVec2,
    var data: RoomData? = null,
) : RoomInfo {
    override val segments: ArrayList<ScanTile> = ArrayList(4)
    override var position: IVec2 = initialPosition
    override var rotation: RoomRotation? = null

    override var shape: RoomShape = RoomShape.OneByOne

    var discovered: Boolean = false
    var clayPos: BlockPos? = null
    var highestBlock: Int? = null
    var waypoints: MutableSet<DungeonWaypoints.DungeonWaypoint> = mutableSetOf()

    var checkmark: MapCheckmark = MapCheckmark.UNDISCOVERED

    val name: String? get() = data?.name

    fun addSegment(segment: DungeonTile) {
        if (!segments.contains(segment)) {
            segments.add(segment)
            position = IVec2(segments.minOf { it.position.x }, segments.minOf { it.position.z })
        }
    }

    fun inferLayoutFromMap() {
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

        rotation = resolveGeometryRotation(positions)
    }

    fun inferLayout(highestBlock: Int) {
        this.highestBlock = highestBlock

        val roomData = data ?: return

        if (applyFairyFallback(highestBlock)) return

        val positions = segments.map { it.position }

        shape = roomData.shape

        if (shape == RoomShape.OneByOne) {
            getRotation()
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

    fun getRotation(): Boolean {
        val y = highestBlock ?: return false
        if (applyFairyFallback(y)) return true
        if (shape != RoomShape.OneByOne) return false

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
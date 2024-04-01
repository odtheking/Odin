package me.odinmain.features.impl.dungeon.puzzlesolvers

import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.utils.addVec
import me.odinmain.utils.plus
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.IceFillFloors.floors
import me.odinmain.utils.skyblock.IceFillFloors.representativeFloors
import me.odinmain.utils.skyblock.PlayerUtils.posFloored
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getBlockIdAt
import me.odinmain.utils.skyblock.isAir
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.fml.common.gameevent.TickEvent

object IceFillSolver {
    var scanned = BooleanArray(3) { false }
    var currentPatterns: MutableList<List<Vec3i>> = ArrayList()
    var renderRotation: Rotation? = null
    var rPos: MutableList<Vec3> = ArrayList()


    private fun renderPattern(pos: Vec3i, rotation: Rotation) {
        renderRotation = rotation
        rPos.add(Vec3(pos.x + 0.5, pos.y + 0.1, pos.z + 0.5))
    }

    fun onRenderWorldLast(color: Color) {
        if (currentPatterns.size == 0 || rPos.size == 0 /*|| DungeonUtils.currentRoomName != "Ice Fill"*/) return
        val rotation = renderRotation ?: return
        for (i in currentPatterns.indices) {
            val pattern = currentPatterns[i]
            val pos = rPos[i]
            //val color = getRainbowColor()
            Renderer.draw3DLine(pos, pos + transformTo(pattern[0], renderRotation!!), color, 10, true)

            for (j in 1 until pattern.size) {
                Renderer.draw3DLine(
                    pos + transformTo(pattern[j - 1], renderRotation!!), pos + transformTo(pattern[j], renderRotation!!), color, 10, true
                )
            }
        }
    }

    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null/* || !DungeonUtils.inDungeons || DungeonUtils.currentRoomName != "Ice Fill"*/) return
        val pos = posFloored
        if (getBlockIdAt(BlockPos(pos.x, pos.y - 1, pos.z )) != 79) return
        val floorIndex = pos.y % 70
        if (floorIndex !in scanned.indices || scanned[floorIndex]) return
        scope.launch {
            val rotation = checkRotation(pos, floorIndex) ?: return@launch
            if (!scan(pos, floorIndex)) return@launch

            scanned[floorIndex] = true
            if (floorIndex == 2) return@launch
            val a = transform(Vec3i(if (floorIndex == 0) 5 else 7, 1, 0), rotation)
            if (!scan(pos.addVec(a.x, a.y, a.z), floorIndex + 1)) return@launch

            scanned[floorIndex + 1] = true
            if (floorIndex == 1) return@launch
            val b = transform(Vec3i(12, 2, 0), rotation)
            scanned[2] = scan(pos.addVec(b.x, b.y, b.z), 2)
        }
    }

    private fun scan(pos: Vec3i, floorIndex: Int): Boolean {
        val rotation = checkRotation(pos, floorIndex) ?: return false

        val bPos = BlockPos(pos)

        val floorHeight = representativeFloors[floorIndex]
        val startTime = System.nanoTime()

        for (index in floorHeight.indices) {
            if (
                isAir(bPos.add(transform(floorHeight[index].first, rotation))) &&
                !isAir(bPos.add(transform(floorHeight[index].second, rotation)))
            ) {
                val scanTime: Double = (System.nanoTime() - startTime) / 1000000.0
                modMessage("Scan took $scanTime ms")

                renderPattern(pos, rotation)
                currentPatterns.add(floors[floorIndex][index].toMutableList())
                return true
            }
        }
        return false
    }

    fun transform(vec: Vec3i, rotation: Rotation): Vec3i {
        return when (rotation) {
            Rotation.EAST -> Vec3i(vec.x, vec.y, vec.z)
            Rotation.WEST -> Vec3i(-vec.x, vec.y, -vec.z)
            Rotation.SOUTH -> Vec3i(vec.z, vec.y, vec.x)
            else -> Vec3i(vec.z, vec.y, -vec.x)
        }
    }

    fun transform(x: Int, z: Int, rotation: Rotation): Pair<Int, Int> {
        return when (rotation) {
            Rotation.EAST -> Pair(x, z)
            Rotation.WEST -> Pair(-x, -z)
            Rotation.SOUTH -> Pair(z, x)
            else -> Pair(z, -x)
        }
    }

    fun transformTo(vec: Vec3i, rotation: Rotation): Vec3 {
        return when (rotation) {
            Rotation.EAST -> Vec3(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())
            Rotation.WEST -> Vec3(-vec.x.toDouble(), vec.y.toDouble(), -vec.z.toDouble())
            Rotation.SOUTH -> Vec3(vec.z.toDouble(), vec.y.toDouble(), vec.x.toDouble())
            else -> Vec3(vec.z.toDouble(), vec.y.toDouble(), -vec.x.toDouble())
        }
    }

    fun checkRotation(pos: Vec3i, floor: Int): Rotation? {
        val a = ((floor + 1) * 2) + 2
        if      (getBlockIdAt(pos.x + a, pos.y, pos.z) == 109) return Rotation.EAST
        else if (getBlockIdAt(pos.x - a, pos.y, pos.z) == 109) return Rotation.WEST
        else if (getBlockIdAt(pos.x, pos.y, pos.z + a) == 109) return Rotation.SOUTH
        else if (getBlockIdAt(pos.x, pos.y, pos.z - a) == 109) return Rotation.NORTH
        return null
    }


    fun onWorldLoad() {
        currentPatterns = ArrayList()
        scanned = BooleanArray(3) { false }
        renderRotation = null
        rPos = ArrayList()
    }
}
enum class Rotation { EAST, WEST, SOUTH, NORTH }
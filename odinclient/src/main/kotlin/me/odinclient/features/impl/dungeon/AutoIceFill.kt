package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.utils.skyblock.PlayerUtils.clipTo
import me.odinclient.utils.waitUntilPacked
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.plus
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i

object AutoIceFill: Module(
    name = "Auto Ice Fill",
    description = "Automatically completes the ice fill puzzle.",
    category = Category.DUNGEON,
    tag = TagType.RISKY
) {

    enum class Rotation {
        EAST, WEST, SOUTH, NORTH
    }

    private suspend fun move(pos: Vec3, pattern: List<Vec3i>, rotation: Rotation, floorIndex: Int) {
        val x = pos.xCoord
        val y = pos.yCoord
        val z = pos.zCoord
        val deferred1 = waitUntilPacked(x, y, z)
        try {
            deferred1.await()
        } catch (e: Exception) {
            return
        }
        val (bx, bz) = transform(pattern[0].x, pattern[0].z, rotation)
        clipTo(x + bx, y + 1, z + bz)
        for (i in 0..pattern.size - 2) {
            val deferred = waitUntilPacked(
                pos + transformTo(pattern[i], rotation)
            )
            try {
                deferred.await()
            } catch (e: Exception) {
                return
            }
            clipTo(
                pos + transformTo(pattern[i + 1], rotation).addVector(0.0, 1.0, 0.0)
            )
        }
        if (floorIndex == 2) return
        val (bx2, bz2) = transform(pattern[pattern.size - 1].x, pattern[pattern.size - 1].z, rotation)
        val deferred = waitUntilPacked(x + bx2, y, z + bz2)
        try {
            deferred.await()
        } catch (e: Exception) {
            return
        }
        clipToNext(pos, rotation, bx2, bz2, floorIndex)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun clipToNext(pos: Vec3, rotation: Rotation, bx: Int, bz: Int, floorIndex: Int) {
        val x = pos.xCoord
        val y = pos.yCoord
        val z = pos.zCoord
        val (nx, ny) = when (rotation) {
            Rotation.EAST -> Pair(0.5f, 0f)
            Rotation.WEST -> Pair(-0.5f, 0f)
            Rotation.SOUTH -> Pair(0f, 0.5f)
            else -> Pair(0f, -0.5f)
        }
        clipTo(x + bx + nx, y + 1.5, z + bz + ny)
        GlobalScope.launch {
            delay(100)
            clipTo(x + bx + nx * 2, y + 2, z + bz + ny * 2)
            delay(100)
            clipTo(x + bx + nx * 4, y + 2, z + bz + ny * 4)
        }
    }

    private fun transform(vec: Vec3i, rotation: Rotation): Vec3i {
        return when (rotation) {
            Rotation.EAST -> Vec3i(vec.x, vec.y, vec.z)
            Rotation.WEST -> Vec3i(-vec.x, vec.y, -vec.z)
            Rotation.SOUTH -> Vec3i(vec.z, vec.y, vec.x)
            else -> Vec3i(vec.z, vec.y, -vec.x)
        }
    }

    private fun transformTo(vec: Vec3i, rotation: Rotation): Vec3 {
        return when (rotation) {
            Rotation.EAST -> Vec3(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())
            Rotation.WEST -> Vec3(-vec.x.toDouble(), vec.y.toDouble(), -vec.z.toDouble())
            Rotation.SOUTH -> Vec3(vec.z.toDouble(), vec.y.toDouble(), vec.x.toDouble())
            else -> Vec3(vec.z.toDouble(), vec.y.toDouble(), -vec.x.toDouble())
        }
    }

    private fun transform(x: Int, z: Int, rotation: Rotation): Pair<Int, Int> {
        return when (rotation) {
            Rotation.EAST -> Pair(x, z)
            Rotation.WEST -> Pair(-x, -z)
            Rotation.SOUTH -> Pair(z, x)
            else -> Pair(z, -x)
        }
    }
}
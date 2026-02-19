package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Rotations
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

data class Vec2(val x: Int, val z: Int)

operator fun Vec3.component1(): Double = x
operator fun Vec3.component2(): Double = y
operator fun Vec3.component3(): Double = z

operator fun BlockPos.component1(): Int = x
operator fun BlockPos.component2(): Int = y
operator fun BlockPos.component3(): Int = z

operator fun Vec3.unaryMinus(): Vec3 = Vec3(-x, -y, -z)

fun Vec3.toBlockPos(): BlockPos =
    BlockPos(floor(x).toInt(), floor(y).toInt(), floor(z).toInt())

fun Vec3.floorVec(): Vec3 =
    Vec3(floor(x), floor(y), floor(z))

fun Vec3.addVec(x: Number = 0.0, y: Number = 0.0, z: Number = 0.0): Vec3 =
    Vec3(this.x + x.toDouble(), this.y + y.toDouble(), this.z + z.toDouble())

/**
 * Rotates a Vec3 around the given rotation.
 * @param rotation The rotation to rotate around
 * @return The rotated Vec3
 */
fun BlockPos.rotateAroundNorth(rotation: Rotations): BlockPos =
    when (rotation) {
        Rotations.NORTH -> BlockPos(-this.x, this.y, -this.z)
        Rotations.WEST ->  BlockPos(-this.z, this.y, this.x)
        Rotations.SOUTH -> BlockPos(this.x, this.y, this.z)
        Rotations.EAST ->  BlockPos(this.z, this.y, -this.x)
        else -> this
    }

/**
 * Rotates a Vec3 to the given rotation.
 * @param rotation The rotation to rotate to
 * @return The rotated Vec3
 */
fun BlockPos.rotateToNorth(rotation: Rotations): BlockPos =
    when (rotation) {
        Rotations.NORTH -> BlockPos(-this.x, this.y, -this.z)
        Rotations.WEST ->  BlockPos(this.z, this.y, -this.x)
        Rotations.SOUTH -> BlockPos(this.x, this.y, this.z)
        Rotations.EAST ->  BlockPos(-this.z, this.y, this.x)
        else -> this
    }

fun isXZInterceptable(box: AABB, range: Double, pos: Vec3, yaw: Float, pitch: Float): Boolean {
    val start = pos.addVec(y = (mc.player?.eyeY ?: 0.0))
    val goal = start.add(getLook(yaw, pitch).multiply(range, range, range))

    return isVecInZ(start.intermediateWithXValue(goal, box.minX), box) ||
            isVecInZ(start.intermediateWithXValue(goal, box.maxX), box) ||
            isVecInX(start.intermediateWithZValue(goal, box.minZ), box) ||
            isVecInX(start.intermediateWithZValue(goal, box.maxZ), box)
}

private fun getLook(yaw: Float, pitch: Float): Vec3 {
    val f2 = -cos(-pitch * 0.017453292f).toDouble()
    return Vec3(
        sin(-yaw * 0.017453292f - 3.1415927f) * f2,
        sin(-pitch * 0.017453292f).toDouble(),
        cos(-yaw * 0.017453292f - 3.1415927f) * f2
    )
}

private fun isVecInX(vec: Vec3?, box: AABB): Boolean =
    vec != null && vec.x >= box.minX && vec.x <= box.maxX

private fun isVecInZ(vec: Vec3?, box: AABB): Boolean =
    vec != null && vec.z >= box.minZ && vec.z <= box.maxZ

private fun Vec3.intermediateWithXValue(goal: Vec3, x: Double): Vec3? {
    val dx = goal.x - this.x
    if (dx * dx < 1e-8) return null
    val t = (x - this.x) / dx
    return if (t in 0.0..1.0) Vec3(
        this.x + dx * t,
        this.y + (goal.y - this.y) * t,
        this.z + (goal.z - this.z) * t
    ) else null
}

private fun Vec3.intermediateWithZValue(goal: Vec3, z: Double): Vec3? {
    val dz = goal.z - this.z
    if (dz * dz < 1e-8) return null
    val t = (z - this.z) / dz
    return if (t in 0.0..1.0) Vec3(
        this.x + (goal.x - this.x) * t,
        this.y + (goal.y - this.y) * t,
        this.z + dz * t
    ) else null
}
package me.odinclient.utils

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.Utils.floor
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


object VecUtils {

    fun noSqrt3DDistance(entity: Entity, entity1: Entity): Double {
        return (entity.posX - entity1.posX).pow(2.0) +
                (entity.posY - entity1.posY).pow(2.0) +
                (entity.posZ - entity1.posZ).pow(2.0)
    }

    fun xzDistance(entity: Entity, entity1: Entity): Double {
        return (entity.posX - entity1.posX).pow(2.0) +
                (entity.posZ - entity1.posZ).pow(2.0)
    }

    private fun fastEyeHeight(): Float {
        return if (mc.thePlayer?.isSneaking == true) 1.54f else 1.62f
    }

    fun isFacingAABB(aabb: AxisAlignedBB, range: Float): Boolean {
        return isInterceptable(aabb, range)
    }

    private fun isInterceptable(aabb: AxisAlignedBB, range: Float): Boolean {
        val player = mc.thePlayer ?: return false
        val position = Vec3(player.posX, player.posY + fastEyeHeight(), player.posZ)
        val f2: Float = -MathHelper.cos(-player.rotationPitch * 0.017453292f)

        val look = Vec3(
            sin(-player.rotationYaw * 0.017453292 - 3.1415927) * f2,
            sin(-player.rotationPitch * 0.017453292),
            cos(-player.rotationYaw * 0.017453292 - 3.1415927) * f2
        )
        return isInterceptable3(
            position,
            position.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range),
            aabb
        )
    }

    private fun isInterceptable3(start: Vec3, goal: Vec3, aabb: AxisAlignedBB): Boolean {

        return try { (
            isVecInYZ(start.getIntermediateWithXValue(goal, aabb.minX), aabb) ||
            isVecInYZ(start.getIntermediateWithXValue(goal, aabb.maxX), aabb) ||
            isVecInXZ(start.getIntermediateWithYValue(goal, aabb.minY), aabb) ||
            isVecInXZ(start.getIntermediateWithYValue(goal, aabb.maxY), aabb) ||
            isVecInXY(start.getIntermediateWithZValue(goal, aabb.minZ), aabb) ||
            isVecInXY(start.getIntermediateWithZValue(goal, aabb.maxZ), aabb)
        ) } catch (e: Exception) { false }
    }

    private fun isVecInYZ(vec: Vec3, aabb: AxisAlignedBB): Boolean =
        vec.yCoord in aabb.minY..aabb.maxY && vec.zCoord in aabb.minZ..aabb.maxZ

    private fun isVecInXZ(vec: Vec3, aabb: AxisAlignedBB): Boolean =
        vec.xCoord in aabb.minX..aabb.maxX && vec.zCoord in aabb.minZ..aabb.maxZ

    private fun isVecInXY(vec: Vec3, aabb: AxisAlignedBB): Boolean =
        vec.xCoord in aabb.minX..aabb.maxX && vec.yCoord in aabb.minY..aabb.maxY

    operator fun Vec3.plus(vec3: Vec3): Vec3 {
        return this.add(vec3)
    }

    fun Vec3.addVec(x: Double = .0, y: Double = .0, z: Double = .0): Vec3 {
        return this.addVector(x, y, z)
    }

    fun Vec3.floored(): Vec3i {
        return Vec3i(xCoord.floor(), yCoord.floor(), zCoord.floor())
    }

    fun scale(vec3: Vec3i,scale: Float): Vec3 {
        return Vec3((vec3.x * scale).toDouble(), (vec3.y* scale).toDouble(), (vec3.z * scale).toDouble())
    }
}
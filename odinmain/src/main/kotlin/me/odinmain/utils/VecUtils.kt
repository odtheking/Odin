package me.odinmain.utils

import me.odinmain.OdinMain.mc
import me.odinmain.utils.skyblock.ChatUtils.devMessage
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


object VecUtils {

    /**
     * Gets the distance between two entities squared.
     */
    fun noSqrt3DDistance(entity: Entity, entity1: Entity): Double {
        return (entity.posX - entity1.posX).pow(2.0) +
                (entity.posY - entity1.posY).pow(2.0) +
                (entity.posZ - entity1.posZ).pow(2.0)
    }

    /**
     * Gets the distance between two entities, ignoring y coordinate, squared.
     */
    fun xzDistance(entity: Entity, entity1: Entity): Double {
        return (entity.posX - entity1.posX).pow(2.0) +
                (entity.posZ - entity1.posZ).pow(2.0)
    }

    private fun fastEyeHeight(): Float {
        return if (mc.thePlayer?.isSneaking == true) 1.54f else 1.62f
    }

    private fun getPositionEyes(): Vec3 {
        return Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.posY + fastEyeHeight(),
            mc.thePlayer.posZ
        )
    }

    private fun getLook(yaw: Float = mc.thePlayer.rotationYaw, pitch: Float = mc.thePlayer.rotationPitch): Vec3 {
        val f2 = -MathHelper.cos(-pitch * 0.017453292f).toDouble()
        return Vec3(
            MathHelper.sin(-yaw * 0.017453292f - 3.1415927f) * f2,
            MathHelper.sin(-pitch * 0.017453292f).toDouble(),
            MathHelper.cos(-yaw * 0.017453292f - 3.1415927f) * f2
        )
    }

    fun isFacingAABB(aabb: AxisAlignedBB, range: Float, yaw: Float = mc.thePlayer.rotationYaw, pitch: Float = mc.thePlayer.rotationPitch): Boolean {
        return isInterceptable(aabb, range, yaw, pitch)
    }

    fun isXZInterceptable(aabb: AxisAlignedBB, range: Float, yaw: Float, pitch: Float): Boolean {
        val position = getPositionEyes()
        val look = getLook(yaw, pitch)
        return isXZInterceptable(
            position,
            position.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range),
            aabb
        )
    }

    private fun isXZInterceptable(start: Vec3, goal: Vec3?, aabb: AxisAlignedBB): Boolean {
        return isVecInZ(start.getIntermediateWithXValue(goal, aabb.minX), aabb) ||
                isVecInZ(start.getIntermediateWithXValue(goal, aabb.maxX), aabb) ||
                isVecInX(start.getIntermediateWithZValue(goal, aabb.minZ), aabb) ||
                isVecInX(start.getIntermediateWithZValue(goal, aabb.maxZ), aabb) /*||
                isVecInXZ(start.getIntermediateWithYValue(goal, 0.0), aabb) ||
                isVecInXZ(start.getIntermediateWithYValue(goal, 255.0), aabb)
                */
    }

    fun Vec3.equal(other: Vec3): Boolean {
        return this.xCoord == other.xCoord && this.yCoord == other.yCoord && this.zCoord == other.zCoord
    }

    private fun isInterceptable(aabb: AxisAlignedBB, range: Float, yaw: Float, pitch: Float): Boolean {
        val player = mc.thePlayer ?: return false
        val position = Vec3(player.posX, player.posY + fastEyeHeight(), player.posZ)

        val f2: Float = -cos(-pitch * 0.017453292f)
        val look = Vec3(
            sin(-yaw * Math.toRadians(1.0) - Math.PI) * f2,
            sin   (-pitch * Math.toRadians(1.0)),
            cos(-yaw * Math.toRadians(1.0) - Math.PI) * f2
        )

        modMessage("look: ${position}, ${position.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range)} $aabb")

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
                ) } catch (e: Exception) {
                    false
                }
    }

    private fun isVecInYZ(vec: Vec3, aabb: AxisAlignedBB): Boolean =
        vec.yCoord in aabb.minY..aabb.maxY && vec.zCoord in aabb.minZ..aabb.maxZ

    private fun isVecInXZ(vec: Vec3, aabb: AxisAlignedBB): Boolean =
        vec.xCoord in aabb.minX..aabb.maxX && vec.zCoord in aabb.minZ..aabb.maxZ

    private fun isVecInXY(vec: Vec3, aabb: AxisAlignedBB): Boolean =
        vec.xCoord in aabb.minX..aabb.maxX && vec.yCoord in aabb.minY..aabb.maxY

    private fun isVecInZ(vec: Vec3?, aabb: AxisAlignedBB): Boolean {
        return vec != null && vec.zCoord >= aabb.minZ && vec.zCoord <= aabb.maxZ
    }

    private fun isVecInX(vec: Vec3?, aabb: AxisAlignedBB): Boolean {
        return vec != null && vec.xCoord >= aabb.minX && vec.xCoord <= aabb.maxX
    }

    operator fun Vec3.plus(vec3: Vec3): Vec3 {
        return this.add(vec3)
    }

    /**
     * Adds the given coordinates to the Vec3.
     */
    fun Vec3.addVec(x: Double = .0, y: Double = .0, z: Double = .0): Vec3 {
        return this.addVector(x, y, z)
    }

    /**
     * Floors every coordinate of a Vec3 and turns it into a Vec3i.
     */
    fun Vec3.floored(): Vec3i {
        return Vec3i(xCoord.floor(), yCoord.floor(), zCoord.floor())
    }

    /**
     * Floors every coordinate of a Vec3
     */
    fun Vec3.flooredVec(): Vec3 {
        return Vec3(xCoord.floor(), yCoord.floor(), zCoord.floor())
    }

    fun BlockPos.toAABB(): AxisAlignedBB {
        return AxisAlignedBB(this.x.toDouble(), this.y.toDouble(), this.z.toDouble(), this.x.toDouble() + 1.0, this.y.toDouble() + 1.0, this.z.toDouble() + 1.0).expand(0.01, 0.01, 0.01)
    }

    /**
     * Scales every coordinate of a Vec3i by the given scale.
     */
    fun scale(vec3: Vec3i,scale: Float): Vec3 {
        return Vec3((vec3.x * scale).toDouble(), (vec3.y* scale).toDouble(), (vec3.z * scale).toDouble())
    }

    /**
     * Clones a Vec3 object.
     */
    fun Vec3.clone(): Vec3 = Vec3(this.xCoord, this.yCoord, this.zCoord)

    /**
     * Turns a Vec3 into a double array.
     */
    fun Vec3.toDoubleArray(): DoubleArray {
        return doubleArrayOf(xCoord, yCoord, zCoord)
    }

    /**
     * Turns a BlockPos into a Vec3i.
     */
    fun BlockPos.toVec3i(): Vec3i {
        return Vec3i(x, y, z)
    }

    /**
     * Turns a double array into a Vec3.
     */
    fun DoubleArray.toVec3(): Vec3 {
        return Vec3(this[0], this[1], this[2])
    }

    /**
     * Solves the equation for diana burrow estimate.
     * @see me.odinclient.utils.skyblock.DianaBurrowEstimate.guessPosition
     * @author Soopy
     */
    fun solveEquationThing(x: Vec3, y: Vec3): Triple<Double, Double, Double> {
        val a = (-y.xCoord * x.yCoord * x.xCoord - y.yCoord * x.yCoord * x.zCoord + y.yCoord * x.yCoord * x.xCoord + x.yCoord * x.zCoord * y.zCoord + x.xCoord * x.zCoord * y.xCoord - x.xCoord * x.zCoord * y.zCoord) / (x.yCoord * y.xCoord - x.yCoord * y.zCoord + x.xCoord * y.zCoord - y.xCoord * x.zCoord + y.yCoord * x.zCoord - y.yCoord * x.xCoord)
        val b = (y.xCoord - y.yCoord) * (x.xCoord + a) * (x.yCoord + a) / (x.yCoord - x.xCoord)
        val c = y.xCoord - b / (x.xCoord + a)
        return Triple(a, b, c)
    }

    /**
     * @return The Vec3 with the given coordinates. If no coordinates are given, the original Vec3 is returned.
     */
    fun Vec3.with(x: Double = this.xCoord, y: Double = this.yCoord, z: Double = this.zCoord): Vec3 {
        return Vec3(x, y, z)
    }

    /**
     * Coerces the given Vec3's x coordinate to be within the given range.
     */
    fun Vec3.coerceXIn(min: Double, max: Double): Vec3 {
        return Vec3(xCoord.coerceIn(min, max), yCoord, zCoord)
    }

    /**
     * Coerces the given Vec3's y coordinate to be within the given range.
     */
    fun Vec3.coerceYIn(min: Double, max: Double): Vec3 {
        return Vec3(xCoord, yCoord.coerceIn(min, max), zCoord)
    }

    /**
     * Coerces the given Vec3's Z coordinate to be within the given range.
     */
    fun Vec3.coerceZIn(min: Double, max: Double): Vec3 {
        return Vec3(xCoord, yCoord, zCoord.coerceIn(min, max))
    }

    /**
     * Gets the Vec3 position of the given S29PacketSoundEffect.
     * @author Bonsai
     */
    val S29PacketSoundEffect.pos: Vec3
        get() = Vec3(this.x, this.y, this.z)


    /**
     * Finds the nearest grass block to the given position.
     * @see me.odinclient.utils.skyblock.DianaBurrowEstimate.guessPosition
     * @param pos The position to search around.
     * @author Bonsai
     */
    fun findNearestGrassBlock(pos: Vec3): Vec3 {
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(pos))
        if (!chunk.isLoaded) return pos.coerceYIn(50.0, 90.0)

        val blocks = List(70) { i -> BlockPos(pos.xCoord, i + 50.0, pos.zCoord) }.filter { chunk.getBlock(it) == Blocks.grass }
        if (blocks.isEmpty()) return pos.coerceYIn(50.0, 90.0)
        return Vec3(blocks.minBy { abs(pos.yCoord - it.y) })
    }
}
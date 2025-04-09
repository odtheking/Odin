package me.odinmain.utils

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.utils.render.RenderUtils.outlineBounds
import me.odinmain.utils.skyblock.dungeon.tiles.Rotations
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.*
import kotlin.math.*

data class Vec2(val x: Int, val z: Int)

data class PositionLook(val pos: Vec3, val yaw: Float, val pitch: Float)

fun BlockPos.add(vec: Vec2): BlockPos = this.add(vec.x, 0, vec.z)

/**
 * Gets the eye height of the player
 * @return The eye height of the player
 */
fun fastEyeHeight(): Float =
    if (mc.thePlayer?.isSneaking == true) 1.54f else 1.62f

/**
 * Gets the position of the player's eyes
 * @param pos The position to get the eyes of
 * @return The position of the player's eyes
 */
fun getPositionEyes(pos: Vec3 = mc.thePlayer?.positionVector ?: Vec3(0.0, 0.0, 0.0)): Vec3 = Vec3(pos.xCoord, pos.yCoord + fastEyeHeight(), pos.zCoord)

/**
 * Gets a normalized vector of the player's look
 * @param yaw The yaw of the player
 * @param pitch The pitch of the player
 * @return A normalized vector of the player's look
 */
fun getLook(yaw: Float = mc.thePlayer?.rotationYaw ?: 0f, pitch: Float = mc.thePlayer?.rotationPitch ?: 0f): Vec3 {
    val f2 = -cos(-pitch * 0.017453292f).toDouble()
    return Vec3(
        sin(-yaw * 0.017453292f - 3.1415927f) * f2,
        sin(-pitch * 0.017453292f).toDouble(),
        cos(-yaw * 0.017453292f - 3.1415927f) * f2
    )
}

/**
 * Returns true if the given position is being looked at by the player within the given range.
 * @param aabb The position to check
 * @param range The range to check
 * @param yaw The yaw of the player
 * @param pitch The pitch of the player
 * @return True if the given position is being looked at by the player within the given range
 */
fun isFacingAABB(aabb: AxisAlignedBB, range: Float, yaw: Float = mc.thePlayer?.rotationYaw ?: 0f, pitch: Float = mc.thePlayer?.rotationPitch ?: 0f): Boolean =
    isInterceptable(aabb, range, yaw, pitch)

operator fun Vec3.component1(): Double = xCoord

operator fun Vec3.component2(): Double = yCoord

operator fun Vec3.component3(): Double = zCoord

operator fun Vec3i.component1(): Int = x

operator fun Vec3i.component2(): Int = y

operator fun Vec3i.component3(): Int = z

/**
 * Returns true if the given position is being looked at by the player within the given range, ignoring the Y value.
 * @param aabb The position to check
 * @param range The range to check
 * @param yaw The yaw of the player
 * @param pitch The pitch of the player
 * @return True if the given position is being looked at by the player within the given range, ignoring the Y value
 */
fun isXZInterceptable(aabb: AxisAlignedBB, range: Float, pos: Vec3, yaw: Float, pitch: Float): Boolean = with(getPositionEyes(pos)) {
    return isXZInterceptable(this, this.add(getLook(yaw, pitch).multiply(range)), aabb)
}

private fun isXZInterceptable(start: Vec3, goal: Vec3?, aabb: AxisAlignedBB): Boolean {
    return  isVecInZ(start.getIntermediateWithXValue(goal, aabb.minX), aabb) ||
            isVecInZ(start.getIntermediateWithXValue(goal, aabb.maxX), aabb) ||
            isVecInX(start.getIntermediateWithZValue(goal, aabb.minZ), aabb) ||
            isVecInX(start.getIntermediateWithZValue(goal, aabb.maxZ), aabb)
}

/**
 * Multiplies every coordinate of a Vec3 by the given factor.
 * @param factor The factor to multiply by
 * @return The multiplied Vec3
 */
fun Vec3.multiply(factor: Number): Vec3 =
    Vec3(this.xCoord * factor.toDouble(), this.yCoord * factor.toDouble(), this.zCoord * factor.toDouble())

fun Vec3.multiply(x: Double = 1.0, y: Double = 1.0, z: Double = 1.0): Vec3 =
    Vec3(this.xCoord * x, this.yCoord * y, this.zCoord * z)

fun Vec3.equal(other: Vec3): Boolean =
    this.xCoord == other.xCoord && this.yCoord == other.yCoord && this.zCoord == other.zCoord

/**
 * Gets the coordinate of the given index of a Vec3. (0 = x, 1 = y, 2 = z)
 * @param index The index to get
 * @return The coordinate of the given index of a Vec3
 */
operator fun Vec3.get(index: Int): Double =
    when (index) {
        0 -> this.xCoord
        1 -> this.yCoord
        2 -> this.zCoord
        else -> throw IndexOutOfBoundsException("Index: $index, Size: 3")
    }

/**
 * Rotates a Vec3 around the given rotation.
 * @param rotation The rotation to rotate around
 * @return The rotated Vec3
 */
fun Vec3.rotateAroundNorth(rotation: Rotations): Vec3 =
    when (rotation) {
        Rotations.NORTH -> Vec3(-this.xCoord, this.yCoord, -this.zCoord)
        Rotations.WEST ->  Vec3(-this.zCoord, this.yCoord, this.xCoord)
        Rotations.SOUTH -> Vec3(this.xCoord, this.yCoord, this.zCoord)
        Rotations.EAST ->  Vec3(this.zCoord, this.yCoord, -this.xCoord)
        else -> this
    }

/**
 * Rotates a Vec3 to the given rotation.
 * @param rotation The rotation to rotate to
 * @return The rotated Vec3
 */
fun Vec3.rotateToNorth(rotation: Rotations): Vec3 =
    when (rotation) {
        Rotations.NORTH -> Vec3(-this.xCoord, this.yCoord, -this.zCoord)
        Rotations.WEST ->  Vec3(this.zCoord, this.yCoord, -this.xCoord)
        Rotations.SOUTH -> Vec3(this.xCoord, this.yCoord, this.zCoord)
        Rotations.EAST ->  Vec3(-this.zCoord, this.yCoord, this.xCoord)
        else -> this
    }

/**
 * Checks if an axis-aligned bounding box (AABB) is interceptable based on the player's position, range, yaw, and pitch.
 *
 * @param aabb The axis-aligned bounding box to check for interceptability.
 * @param range The range of the interception.
 * @param yaw The yaw angle.
 * @param pitch The pitch angle.
 * @return `true` if the AABB is interceptable, `false` otherwise.
 */
private fun isInterceptable(aabb: AxisAlignedBB, range: Float, yaw: Float, pitch: Float): Boolean = with(getPositionEyes()) {
    return isInterceptable3(this, this.add(getLook(yaw, pitch).multiply(range)), aabb)
}

/**
 * Checks if an axis-aligned bounding box (AABB) is interceptable between two points.
 *
 * @param start The starting point.
 * @param goal The ending point.
 * @param aabb The axis-aligned bounding box to check for interceptability.
 * @return `true` if the AABB is interceptable, `false` otherwise.
 */
private fun isInterceptable3(start: Vec3, goal: Vec3, aabb: AxisAlignedBB): Boolean {
    return try {
        (
            isVecInYZ(start.getIntermediateWithXValue(goal, aabb.minX), aabb) ||
            isVecInYZ(start.getIntermediateWithXValue(goal, aabb.maxX), aabb) ||
            isVecInXZ(start.getIntermediateWithYValue(goal, aabb.minY), aabb) ||
            isVecInXZ(start.getIntermediateWithYValue(goal, aabb.maxY), aabb) ||
            isVecInXY(start.getIntermediateWithZValue(goal, aabb.minZ), aabb) ||
            isVecInXY(start.getIntermediateWithZValue(goal, aabb.maxZ), aabb)
        )
    } catch (_: Exception) { false }
}

fun isVecInAABB(vec: Vec3, aabb: AxisAlignedBB): Boolean =
    vec.xCoord in aabb.minX..aabb.maxX && vec.yCoord in aabb.minY..aabb.maxY && vec.zCoord in aabb.minZ..aabb.maxZ

/**
 * Checks if a Vec3 is within the YZ bounds of an axis-aligned bounding box (AABB).
 *
 * @param vec The Vec3 to check.
 * @param aabb The axis-aligned bounding box.
 * @return `true` if the Vec3 is within the YZ bounds, `false` otherwise.
 */
private fun isVecInYZ(vec: Vec3, aabb: AxisAlignedBB): Boolean =
    vec.yCoord in aabb.minY..aabb.maxY && vec.zCoord in aabb.minZ..aabb.maxZ

/**
 * Checks if a Vec3 is within the XZ bounds of an axis-aligned bounding box (AABB).
 *
 * @param vec The Vec3 to check.
 * @param aabb The axis-aligned bounding box.
 * @return `true` if the Vec3 is within the XZ bounds, `false` otherwise.
 */
fun isVecInXZ(vec: Vec3, aabb: AxisAlignedBB): Boolean =
    vec.xCoord in aabb.minX..aabb.maxX && vec.zCoord in aabb.minZ..aabb.maxZ

/**
 * Checks if a Vec3 is within the XY bounds of an axis-aligned bounding box (AABB).
 *
 * @param vec The Vec3 to check.
 * @param aabb The axis-aligned bounding box.
 * @return `true` if the Vec3 is within the XY bounds, `false` otherwise.
 */
private fun isVecInXY(vec: Vec3, aabb: AxisAlignedBB): Boolean =
    vec.xCoord in aabb.minX..aabb.maxX && vec.yCoord in aabb.minY..aabb.maxY

/**
 * Checks if a Vec3 is within the Z bounds of an axis-aligned bounding box (AABB).
 *
 * @param vec The Vec3 to check.
 * @param aabb The axis-aligned bounding box.
 * @return `true` if the Vec3 is within the Z bounds, `false` otherwise.
 */
private fun isVecInZ(vec: Vec3?, aabb: AxisAlignedBB): Boolean =
    vec != null && vec.zCoord >= aabb.minZ && vec.zCoord <= aabb.maxZ

/**
 * Checks if a Vec3 is within the X bounds of an axis-aligned bounding box (AABB).
 *
 * @param vec The Vec3 to check.
 * @param aabb The axis-aligned bounding box.
 * @return `true` if the Vec3 is within the X bounds, `false` otherwise.
 */
private fun isVecInX(vec: Vec3?, aabb: AxisAlignedBB): Boolean =
    vec != null && vec.xCoord >= aabb.minX && vec.xCoord <= aabb.maxX

/**
 * Overloads the `plus` operator for Vec3 to provide vector addition.
 *
 * @param vec3 The Vec3 to add to the current Vec3.
 * @return A new Vec3 representing the sum of the two vectors.
 */
operator fun Vec3.plus(vec3: Vec3): Vec3 =
    this.add(vec3)

/**
 * Adds the given coordinates to the Vec3.
 */
fun Vec3.addVec(x: Number = .0, y: Number = .0, z: Number = .0): Vec3 =
    this.addVector(x.toDouble(), y.toDouble(), z.toDouble())

/**
 * Removes the given coordinates to the Vec3.
 */
fun Vec3.subtractVec(x: Number = .0, y: Number = .0, z: Number = .0): Vec3 =
    this.addVector(-x.toDouble(), -y.toDouble(), -z.toDouble())

/**
 * Adds the given coordinates to the Vec3.
 */
fun Vec3i.addVec(x: Number = .0, y: Number = .0, z: Number = .0): Vec3i =
    Vec3i(this.x + x.toInt(), this.y + y.toInt(), this.z + z.toInt())

/**
 * Floors every coordinate of a Vec3
 */
fun Vec3.floorVec(): Vec3 =
    Vec3(floor(xCoord), floor(yCoord), floor(zCoord))

/**
 * @param add Will determine the maximum bounds
 */
fun BlockPos.toAABB(add: Double = 1.0): AxisAlignedBB =
    AxisAlignedBB(this.x.toDouble(), this.y.toDouble(), this.z.toDouble(), this.x + add, this.y + add, this.z + add).outlineBounds()

/**
 * @param add Will determine the maximum bounds
 */
fun Vec3.toAABB(add: Double = 1.0): AxisAlignedBB =
    AxisAlignedBB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + add, this.yCoord + add, this.zCoord + add).outlineBounds()

/**
 * Turns a Vec3 into a BlockPos.
 */
fun Vec3.toBlockPos(add: Double = 0.0): BlockPos =
    BlockPos(this.xCoord + add, this.yCoord + add, this.zCoord + add)

/**
 * Clones a Vec3 object.
 */
fun Vec3.clone(): Vec3 =
    Vec3(this.xCoord, this.yCoord, this.zCoord)

/**
 * Turns a Vec3 into a double array.
 */
fun Vec3.toDoubleArray(): DoubleArray =
    doubleArrayOf(xCoord, yCoord, zCoord)

/**
 * Turns a BlockPos into a Vec3.
 */
fun BlockPos.toVec3(): Vec3 =
    Vec3(x.toDouble(), y.toDouble(), z.toDouble())

/**
 * Turns a double array into a Vec3.
 */
fun DoubleArray.toVec3(): Vec3 =
    Vec3(this[0], this[1], this[2])

/**
 * Solves the equation for diana burrow estimate.
 * @see me.odinmain.utils.skyblock.DianaBurrowEstimate.guessPosition
 * @author Soopy
 */
fun calculateCoefficientsFromVectors(x: Vec3, y: Vec3): Triple<Double, Double, Double> {
    val a = (-y.xCoord * x.yCoord * x.xCoord - y.yCoord * x.yCoord * x.zCoord + y.yCoord * x.yCoord * x.xCoord + x.yCoord * x.zCoord * y.zCoord + x.xCoord * x.zCoord * y.xCoord - x.xCoord * x.zCoord * y.zCoord) / (x.yCoord * y.xCoord - x.yCoord * y.zCoord + x.xCoord * y.zCoord - y.xCoord * x.zCoord + y.yCoord * x.zCoord - y.yCoord * x.xCoord)
    val b = (y.xCoord - y.yCoord) * (x.xCoord + a) * (x.yCoord + a) / (x.yCoord - x.xCoord)
    val c = y.xCoord - b / (x.xCoord + a)
    return Triple(a, b, c)
}

/**
 * Coerces the given Vec3's y coordinate to be within the given range.
 */
fun Vec3.coerceYIn(min: Double, max: Double): Vec3 =
    Vec3(xCoord, yCoord.coerceIn(min, max), zCoord)

/**
 * Gets the Vec3 position of the given S29PacketSoundEffect.
 * @author Bonsai
 */
inline val S29PacketSoundEffect.positionVector: Vec3 get() =
    Vec3(this.x, this.y, this.z)

inline val S2APacketParticles.positionVector: Vec3 get() =
    Vec3(this.xCoordinate, this.yCoordinate, this.zCoordinate)

inline val AxisAlignedBB.corners: List<Vec3> get() =
    listOf(
        Vec3(minX, minY, minZ), Vec3(minX, maxY, minZ), Vec3(maxX, maxY, minZ), Vec3(maxX, minY, minZ),
        Vec3(minX, minY, maxZ), Vec3(minX, maxY, maxZ), Vec3(maxX, maxY, maxZ), Vec3(maxX, minY, maxZ)
    )

operator fun Vec3.unaryMinus(): Vec3 =
    Vec3(-xCoord, -yCoord, -zCoord)

fun AxisAlignedBB.offset(vec: Vec3) =
    AxisAlignedBB(this.minX + vec.xCoord, this.minY + vec.yCoord, this.minZ + vec.zCoord, this.maxX + vec.xCoord, this.maxY + vec.yCoord, this.maxZ + vec.zCoord)

inline val AxisAlignedBB.middle: Vec3 get() =
    Vec3(this.minX + (this.maxX - this.minX) / 2, this.minY + (this.maxY - this.minY) / 2, this.minZ + (this.maxZ - this.minZ) / 2)

/**
 * Finds the nearest grass block to the given position.
 * @see me.odinmain.utils.skyblock.DianaBurrowEstimate.guessPosition
 * @param pos The position to search around.
 * @author Bonsai
 */
fun findNearestGrassBlock(pos: Vec3): Vec3 {
    val chunk = mc.theWorld?.getChunkFromBlockCoords(BlockPos(pos)) ?: return pos.coerceYIn(50.0, 110.0)
    if (!chunk.isLoaded) return pos.coerceYIn(50.0, 110.0)

    val blocks = List(70) { i -> BlockPos(pos.xCoord, i + 50.0, pos.zCoord) }.filter { chunk.getBlock(it) == Blocks.grass }
    if (blocks.isEmpty()) return pos.coerceYIn(50.0, 109.0)
    return Vec3(blocks.minBy { abs(pos.yCoord - it.y) })
}

/**
 * Returns Triple(distance, yaw, pitch) in minecraft coordinate system to get from x0y0z0 to x1y1z1.
 *
 * @param vec3 Vec3 to get direction to.
 *
 * @param vec31 Vec3 to get direction from.
 *
 * @return Triple of distance, yaw, pitch
 * @author Aton
 */
fun getDirection(vec3: Vec3, vec31: Vec3): Triple<Double, Float, Float> {
    val dist = sqrt((vec31.xCoord - vec3.xCoord).pow(2) + (vec31.yCoord - vec3.yCoord).pow(2) + (vec31.zCoord - vec3.zCoord).pow(2))
    val yaw = -atan2((vec31.xCoord - vec3.xCoord), (vec31.zCoord - vec3.zCoord)) / Math.PI * 180
    val pitch = -atan2((vec31.yCoord - vec3.yCoord), sqrt((vec31.xCoord - vec3.xCoord).pow(2) + (vec31.zCoord - vec3.zCoord).pow(2))) / Math.PI * 180
    return Triple(dist, yaw.toFloat() % 360f, pitch.toFloat() % 360f)
}

/**
 * Returns Triple(distance, yaw, pitch) in minecraft coordinate system to get from player's eyes to Vec3.
 *
 * @param pos Vec3 to get direction to.
 *
 * @return Triple of distance, yaw, pitch
 * @author Bonsai
 */
fun getDirectionToVec3(pos: Vec3): Triple<Double, Float, Float> =
     getDirection(getPositionEyes(), pos)

/**
 * Returns a triple of distance, yaw, pitch to rotate to the given position with etherwarp physics, or null if etherwarp is not possible.
 *
 * @param targetPos The position to rotate to.
 * @return A triple of distance, yaw, pitch to rotate to the given position with etherwarp physics, or null if etherwarp is not possible
 * @see getDirection
 * @author Aton
 */
fun etherwarpRotateTo(targetPos: BlockPos, dist: Double = 61.0): Triple<Double, Float, Float>? {
    val distance = mc.thePlayer?.getDistanceSq(targetPos) ?: return null

    if (distance > (dist + 2) * (dist + 2)) return null

    // check whether the block can be seen or is to far away
    val targets = listOf(
        Vec3(targetPos).add(Vec3(0.5, 1.0, 0.5)),
        Vec3(targetPos).add(Vec3(0.0, 0.5, 0.5)),
        Vec3(targetPos).add(Vec3(0.5, 0.5, 0.0)),
        Vec3(targetPos).add(Vec3(1.0, 0.5, 0.5)),
        Vec3(targetPos).add(Vec3(0.5, 0.5, 1.0)),
        Vec3(targetPos).add(Vec3(0.5, 0.0, 0.5)),

        // corners of the block
        Vec3(targetPos).add(Vec3(0.1, 0.0, 0.1)),
        Vec3(targetPos).add(Vec3(0.1, 1.0, 0.1)),

        Vec3(targetPos).add(Vec3(1.0, 0.0, 0.1)),
        Vec3(targetPos).add(Vec3(1.0, 1.0, 0.1)),

        Vec3(targetPos).add(Vec3(0.1, 0.0, 1.0)),
        Vec3(targetPos).add(Vec3(0.1, 1.0, 1.0)),

        Vec3(targetPos).add(Vec3(0.9, 1.0, 1.0)),
        Vec3(targetPos).add(Vec3(1.0, 1.0, 0.9)),
        Vec3(targetPos).add(Vec3(0.9, 1.0, 1.0))
    )

    var target: Vec3? = null
    val eyeVec = getPositionEyes()

    for (targetVec in targets) {
        val vec32 = eyeVec.add(targetVec.subtract(eyeVec).normalize().multiply(dist))
        // TODO: Make this use etherwarp raytracing, not default minecraft (Take from EtherWarpHelper)
        val obj = mc.theWorld?.rayTraceBlocks(eyeVec, vec32, true, false, true) ?: return null
        if (obj.blockPos == targetPos) {
            target = targetVec
            break
        }
    }

    return target?.let { getDirection(getPositionEyes(), target) }
}

/**
 * Smoothly rotates the players head to the given yaw and pitch.
 *
 * @param yaw The yaw to rotate to
 * @param pitch The pitch to rotate to
 * @param rotTime how long the rotation should take. In milliseconds.
 */
@OptIn(ObsoleteCoroutinesApi::class)
fun smoothRotateTo(yaw: Float, pitch: Float, rotTime: Number, functionToRunWhenDone: () -> Unit = {}) {
    scope.launch {
        val initialYaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)
        val initialPitch = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch)
        val targetYaw = wrapAngle(yaw)
        val targetPitch = wrapAngle(pitch)
        val startTime = System.currentTimeMillis()
        val duration = rotTime.toInt().coerceIn(10, 10000)

        val tickerChannel = ticker(delayMillis = 1, initialDelayMillis = 0)
        for (event in tickerChannel) {
            val currentTime = System.currentTimeMillis()
            val progress = ((currentTime - startTime).toFloat() / duration).coerceIn(0f, 1f)
            val amount = bezier(progress, 0f, 1f, 1f, 1f)

            mc.thePlayer?.rotationYaw = initialYaw + (targetYaw - initialYaw) * amount
            mc.thePlayer?.rotationPitch = initialPitch + (targetPitch - initialPitch) * amount

            if (progress >= 1f) {
                tickerChannel.cancel()
                break
            }
        }

        mc.thePlayer?.rotationYaw = yaw
        mc.thePlayer?.rotationPitch = pitch
        functionToRunWhenDone.invoke()
    }
}

fun wrapAngle(angle: Float): Float {
    var newAngle = angle
    while (newAngle >= 180f) newAngle -= 360f
    while (newAngle < -180f) newAngle += 360f
    return newAngle
}

fun bezier(t: Float, initial: Float, p1: Float, p2: Float, final: Float): Float =
    (1 - t).pow(3) * initial + 3 * (1 - t).pow(2) * t * p1 + 3 * (1 - t) * t.pow(2) * p2 + t.pow(3) * final

fun BlockPos.addRotationCoords(rotation: Rotations, x: Int, z: Int): BlockPos =
    when (rotation) {
        Rotations.NORTH -> BlockPos(this.x + x, this.y, this.z + z)
        Rotations.SOUTH -> BlockPos(this.x - x, this.y, this.z - z)
        Rotations.WEST ->  BlockPos(this.x + z, this.y, this.z - x)
        Rotations.EAST ->  BlockPos(this.x - z, this.y, this.z + x)
        else -> this
    }
package me.odinmain.utils.skyblock

import me.odinmain.features.impl.skyblock.DianaHelper
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

object DianaBurrowEstimate {

    private var numberOfDings = 0
    private var lastDingTime = 0L
    private var lastDingPitch = 0f
    private var firstPitch = 0f
    private var lastParticlePosition: Vec3? = null
    private var secondLastParticlePosition: Vec3? = null
    private var firstParticlePoint: Vec3? = null
    private var currentParticlePosition: Vec3? = null
    private var estimatedBurrowPosition: Vec3? = null

    private var lastSoundPoint: Vec3? = null
    private val particlePositions = mutableListOf<Vec3>()
    private var lastDugBurrow: BlockPos? = null
    private val recentBurrows = mutableSetOf<BlockPos>()
    private var pendingBurrow: BlockPos? = null
    val activeBurrows = ConcurrentHashMap<BlockPos, Burrow>()

    private val dingPitchSlopes = mutableListOf<Float>()

    private var estimatedBurrowDistance: Double? = null

    fun handleBurrow(it: S2APacketParticles) {
        val particleType = ParticleType.getParticleType(it) ?: return
        val location = BlockPos(it.xCoordinate, it.yCoordinate - 1, it.zCoordinate).takeIf { it !in recentBurrows } ?: return
        val burrow = activeBurrows.getOrPut(location) { Burrow(location) }

        burrow.apply {
            when (particleType) {
                ParticleType.FOOTSTEP -> hasFootstep = true
                ParticleType.ENCHANT -> hasEnchant = true
                ParticleType.EMPTY -> type = BurrowType.START
                ParticleType.MOB -> type = BurrowType.MOB
                ParticleType.TREASURE -> type = BurrowType.TREASURE
            }
        }

        if (burrow.hasEnchant && burrow.hasFootstep && burrow.type != BurrowType.UNKNOWN && !burrow.found) burrow.found = true
    }

    fun blockEvent(pos: BlockPos) {
        if (pos !in activeBurrows.keys || !isHolding("ANCESTRAL_SPADE")) return
        lastDugBurrow = pos
        if (pos != pendingBurrow) return
        pendingBurrow = null
        removeBurrow(pos, force = true)
    }

    fun onBurrowDug() {
        lastDugBurrow?.let { if (!removeBurrow(it)) pendingBurrow = it }
    }

    private fun removeBurrow(location: BlockPos, force: Boolean = false): Boolean {
        return activeBurrows[location]?.takeIf { it.found || force }?.let {
            activeBurrows.remove(location)
            recentBurrows.add(location)
            lastDugBurrow = null
            runIn(1200) { if (location in recentBurrows) recentBurrows.remove(location) }
            true
        } == true
    }

    fun handleSoundPacket(packetSound: S29PacketSoundEffect) {
        if (packetSound.soundName != "note.harp" || packetSound.volume != 1f) return

        lastDingTime = System.currentTimeMillis()

        if (lastDingTime == 0L) firstPitch = packetSound.pitch
        if (packetSound.pitch < lastDingPitch) reset()

        if (lastDingPitch == 0f) {
            lastDingPitch = packetSound.pitch
            lastParticlePosition = null
            secondLastParticlePosition = null
            lastSoundPoint = null
            firstParticlePoint = null
            particlePositions.clear()
            return
        }

        numberOfDings++
        if (numberOfDings > 1) dingPitchSlopes.add(packetSound.pitch - lastDingPitch)
        if (dingPitchSlopes.size > 20) dingPitchSlopes.removeFirst()
        lastSoundPoint = packetSound.positionVector
        lastDingPitch = packetSound.pitch

        val firstPosition = firstParticlePoint ?: return

        estimatedBurrowDistance = (Math.E / if (dingPitchSlopes.isNotEmpty()) dingPitchSlopes.average() else 0.0) - firstPosition.distanceTo(packetSound.positionVector)

        if (estimatedBurrowDistance?.let { it > 1000 } == true) {
            estimatedBurrowDistance = null
            return
        }

        val secondLastParticle = secondLastParticlePosition ?: return

        estimatedBurrowDistance?.let { distance ->
            estimatedBurrowPosition = lastSoundPoint?.add(currentParticlePosition?.subtract(secondLastParticle)?.normalize()?.multiply(distance))
        }
    }

    fun handleParticlePacket(packet: S2APacketParticles) {
        if (packet.particleType != EnumParticleTypes.DRIP_LAVA) return
        val currLoc = packet.positionVector

        if (lastSoundPoint?.let { abs(currLoc.xCoord - it.xCoord) < 2 && abs(currLoc.yCoord - it.yCoord) < 0.5 && abs(currLoc.zCoord - it.zCoord) < 2 } != true) return

        guessPosition(currLoc)

        if (lastParticlePosition == null) firstParticlePoint = currLoc.clone()
        secondLastParticlePosition = lastParticlePosition
        lastParticlePosition = currentParticlePosition
        currentParticlePosition = currLoc.clone()

        val estimatedDistance = estimatedBurrowDistance ?: return
        val secondParticlePosition = secondLastParticlePosition ?: return
        val changes = currentParticlePosition?.subtract(secondParticlePosition)?.normalize() ?: return

        lastParticlePosition?.let {
            estimatedBurrowPosition = it.add(changes.multiply(estimatedDistance, 1.0, estimatedDistance))
        }
    }

    private fun guessPosition(currLoc: Vec3) {
        if (particlePositions.size >= 100 || particlePositions.isNotEmpty() && particlePositions.last().distanceTo(currLoc) == 0.0) return

        val distMultiplier = particlePositions.takeIf { it.size > 2 }?.let { currLoc.distanceTo(it.last()) / (0.06507 * it.size + 0.259) } ?: 1.0

        particlePositions.add(currLoc)
        if (particlePositions.size <= 5 || estimatedBurrowPosition == null) return

        val slopeValues = particlePositions.asSequence().zipWithNext { a, b -> atan((a.xCoord - b.xCoord) / (a.zCoord - b.zCoord)) }.toList()
        val (a, b, c) = calculateCoefficientsFromVectors(
            Vec3(slopeValues.size - 5.0, slopeValues.size - 3.0, slopeValues.size - 1.0),
            Vec3(slopeValues[slopeValues.size - 5], slopeValues[slopeValues.size - 3], slopeValues[slopeValues.size - 1])
        )

        val pr1 = mutableListOf<Vec3>()
        val pr2 = mutableListOf<Vec3>()
        val start = slopeValues.size - 1
        val lastPos = particlePositions[start].toDoubleArray()
        val lastPos2 = particlePositions[start].toDoubleArray()

        var distCovered = 0.0

        val ySpeed = (particlePositions.last().xCoord - particlePositions[particlePositions.size - 2].xCoord) /
                hypot(
                    particlePositions.last().xCoord - particlePositions[particlePositions.size - 2].xCoord,
                    particlePositions.last().zCoord - particlePositions[particlePositions.size - 2].zCoord
                )

        val estimatedDistance = estimatedBurrowDistance ?: return

        var i = start + 1
        while (distCovered < estimatedDistance && i < 10000) {
            val y = b / (i + a) + c
            val dist = distMultiplier * (0.06507 * i + 0.259)
            val xOff = dist * sin(y)
            val zOff = dist * cos(y)

            repeat(5) {
                lastPos[0] += xOff / 5
                lastPos[2] += zOff / 5
                lastPos[1] += ySpeed * dist / 5

                lastPos2[0] -= xOff / 5
                lastPos2[2] -= zOff / 5
                lastPos2[1] += ySpeed * dist / 5

                pr1.add(lastPos.toVec3())
                pr2.add(lastPos2.toVec3())

                lastSoundPoint?.let { distCovered = hypot(lastPos[0] - it.xCoord, lastPos[2] - it.zCoord) }
                if (distCovered > estimatedDistance) return@repeat
            }
            i++
        }

        if (pr1.isEmpty()) return

        val p1 = pr1.last()
        val p2 = pr2.last()

        estimatedBurrowPosition?.let {
            DianaHelper.renderPos = findNearestGrassBlock(
                if (((p1.xCoord - it.xCoord) * (2 + p1.zCoord - it.zCoord)).pow(2) < ((p2.xCoord - it.xCoord) * (2 + p2.zCoord - it.zCoord)).pow(2))
                    Vec3(floor(p1.xCoord), 120.0, floor(p1.zCoord))
                else Vec3(floor(p2.xCoord), 120.0, floor(p2.zCoord))
            )
        }
    }

    private fun reset() {
        secondLastParticlePosition = null
        currentParticlePosition = null
        estimatedBurrowPosition = null
        lastParticlePosition = null
        firstParticlePoint = null
        lastSoundPoint = null
        lastDingPitch = 0f
        numberOfDings = 0
        lastDingTime = 0L
        firstPitch = 0f
        dingPitchSlopes.clear()
    }

    fun onWorldLoad() {
        activeBurrows.clear()
        pendingBurrow = null
        recentBurrows.clear()
        reset()
    }

    private enum class ParticleType(val check: S2APacketParticles.() -> Boolean) {
        EMPTY   ({ particleType == EnumParticleTypes.CRIT_MAGIC && particleCount == 4 && particleSpeed == .01f && xOffset == .5f && yOffset == .1f && zOffset == .5f }),
        MOB     ({ particleType == EnumParticleTypes.CRIT && particleCount == 3 && particleSpeed == .01f && xOffset == .5f && yOffset == .1f && zOffset == .5f }),
        TREASURE({ particleType == EnumParticleTypes.DRIP_LAVA && particleCount == 2 && particleSpeed == .01f && xOffset == .35f && yOffset == .1f && zOffset == .35f }),
        FOOTSTEP({ particleType == EnumParticleTypes.FOOTSTEP && particleCount == 1 && particleSpeed == 0f && xOffset == .05f && yOffset == 0f && zOffset == .05f }),
        ENCHANT ({ particleType == EnumParticleTypes.ENCHANTMENT_TABLE && particleCount == 5 && particleSpeed == .05f && xOffset == .5f && yOffset == .4f && zOffset == .5f });

        companion object {
            fun getParticleType(packet: S2APacketParticles): ParticleType? =
                if (!packet.isLongDistance) null else entries.find { it.check(packet) }
        }
    }

    data class Burrow(
        var location: BlockPos,
        var hasFootstep: Boolean = false,
        var hasEnchant: Boolean = false,
        var type: BurrowType = BurrowType.UNKNOWN,
        var found: Boolean = false,
    )

    enum class BurrowType(val text: String, val color: Color) {
        START("§aStart", Colors.MINECRAFT_GREEN),
        MOB("§cMob", Colors.MINECRAFT_RED),
        TREASURE("§6Treasure", Colors.MINECRAFT_GOLD),
        UNKNOWN("§fUnknown?!", Colors.WHITE),
    }
}
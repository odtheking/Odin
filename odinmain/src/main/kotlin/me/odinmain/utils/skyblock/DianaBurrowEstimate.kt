package me.odinmain.utils.skyblock

import me.odinmain.features.impl.skyblock.DianaHelper
import me.odinmain.utils.*
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.*
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
    private var particlePositions = mutableListOf<Vec3>()
    private val burrows = mutableMapOf<Vec3i, Burrow>()
    private var lastBurrow: Vec3i? = null
    val recentBurrows = mutableListOf<Vec3i>()

    private var dingPitchSlopes = mutableListOf<Float>()

    private var estimatedBurrowDistance: Double? = null

    fun handleBurrow(it: S2APacketParticles) {
        val particleType = ParticleType.getParticleType(it) ?: return
        val location = Vec3i(it.xCoordinate, it.yCoordinate - 1, it.zCoordinate)
        val burrow = burrows.getOrPut(location) { Burrow(location) }

        burrow.apply {
            when (particleType) {
                ParticleType.FOOTSTEP -> hasFootstep = true
                ParticleType.ENCHANT -> hasEnchant = true
                ParticleType.EMPTY -> type = 0
                ParticleType.MOB -> type = 1
                ParticleType.TREASURE -> type = 2
            }
        }

        if (!burrow.hasEnchant || !burrow.hasFootstep || burrow.type == -1 || burrow.found || location in recentBurrows) return

        DianaHelper.burrowsRender[burrow.location] = burrow.getType()
        burrow.found = true
    }

    fun blockEvent(pos: Vec3i, isFullyBroken: Boolean = false) {
        if (isFullyBroken) {
            burrows.remove(pos)
            DianaHelper.burrowsRender.remove(pos)
        }
        if (pos !in burrows.keys || !isHolding("ANCESTRAL_SPADE")) return
        lastBurrow = pos
    }

    fun chat(message: String) {
        if (!message.startsWith("You dug out a Griffin Burrow!") && message != "You finished the Griffin burrow chain! (4/4)") return

       lastBurrow?.let {
           recentBurrows.add(it)
           burrows.remove(it)
           DianaHelper.burrowsRender.remove(it)
           lastBurrow = null
        }
    }

    fun handleSoundPacket(it: S29PacketSoundEffect) {
        if (it.soundName != "note.harp" || !LocationUtils.currentArea.isArea(Island.Hub)) return

        if (lastDingTime == 0L) firstPitch = it.pitch

        lastDingTime = System.currentTimeMillis()

        if (it.pitch < lastDingPitch) reset()

        if (lastDingPitch == 0f) {
            lastDingPitch = it.pitch
            lastParticlePosition = null
            secondLastParticlePosition = null
            lastSoundPoint = null
            firstParticlePoint = null
            particlePositions.clear()
            return
        }

        numberOfDings++
        if (numberOfDings > 1) dingPitchSlopes.add(it.pitch - lastDingPitch)
        if (dingPitchSlopes.size > 20) dingPitchSlopes.removeFirst()
        val slope = if (dingPitchSlopes.isNotEmpty()) dingPitchSlopes.average() else 0.0
        lastSoundPoint = it.pos
        lastDingPitch = it.pitch

        if (secondLastParticlePosition == null || currentParticlePosition == null || firstParticlePoint == null) return

        estimatedBurrowDistance = (Math.E / slope) - firstParticlePoint?.distanceTo(it.pos)!!

        if (estimatedBurrowDistance!! > 1000) {
            estimatedBurrowDistance = null
            return
        }

        val lineDist = secondLastParticlePosition?.distanceTo(currentParticlePosition) ?: return

        val changesHelp = currentParticlePosition?.subtract(secondLastParticlePosition) ?: return
        val changes = listOf(changesHelp.xCoord, changesHelp.yCoord, changesHelp.zCoord).map { it / lineDist }

        lastSoundPoint?.let {
            estimatedBurrowPosition = Vec3(it.xCoord + changes[0] * estimatedBurrowDistance!!, it.yCoord + changes[1] * estimatedBurrowDistance!!, it.zCoord + changes[2] * estimatedBurrowDistance!!)
        }
    }


    fun handleParticlePacket(it: S2APacketParticles) {
        if (it.particleType != EnumParticleTypes.DRIP_LAVA) return
        val currLoc = Vec3(it.xCoordinate, it.yCoordinate, it.zCoordinate)

        var run = false
        lastSoundPoint?.let {
            if (abs(currLoc.xCoord - it.xCoord) < 2 && abs(currLoc.yCoord - it.yCoord) < 0.5 && abs(currLoc.zCoord - it.zCoord) < 2) run = true
        }
        if (!run) return

        guessPosition(currLoc)

        if (lastParticlePosition == null) firstParticlePoint = currLoc.clone()
        secondLastParticlePosition = lastParticlePosition
        lastParticlePosition = currentParticlePosition
        currentParticlePosition = currLoc.clone()

        if (secondLastParticlePosition == null || firstParticlePoint == null || estimatedBurrowDistance == null || lastSoundPoint == null) return
        val lineDist = secondLastParticlePosition?.distanceTo(currentParticlePosition) ?: return

        val changesHelp = currentParticlePosition?.subtract(secondLastParticlePosition) ?: return
        val changes = listOf(changesHelp.xCoord, changesHelp.yCoord, changesHelp.zCoord).map { it / lineDist }

        lastParticlePosition?.let {
            estimatedBurrowPosition = Vec3(it.xCoord + changes[0] * estimatedBurrowDistance!!, it.yCoord + changes[1], it.zCoord + changes[2] * estimatedBurrowDistance!!)
        }
    }

    private fun guessPosition(currLoc: Vec3) {
        if (particlePositions.size < 100 && particlePositions.isEmpty() || particlePositions.last().distanceTo(currLoc) != 0.0) {
            var distMultiplier = 1.0
            if (particlePositions.size > 2) {
                val predictedDist = 0.06507 * particlePositions.size + 0.259
                val lastPos = particlePositions.last()
                val actualDist = currLoc.distanceTo(lastPos)
                distMultiplier = actualDist / predictedDist
            }
            particlePositions.add(currLoc)

            if (particlePositions.size > 5 && estimatedBurrowPosition != null) {
                val slopeThing = particlePositions.zipWithNext { a, b -> atan((a.xCoord - b.xCoord) / (a.zCoord - b.zCoord)) }
                val (a, b, c) = solveEquationThing(
                    Vec3(slopeThing.size - 5.0, slopeThing.size - 3.0, slopeThing.size - 1.0),
                    Vec3(slopeThing[slopeThing.size - 5], slopeThing[slopeThing.size - 3], slopeThing[slopeThing.size - 1])
                )

                val pr1 = mutableListOf<Vec3>()
                val pr2 = mutableListOf<Vec3>()

                val start = slopeThing.size - 1
                val lastPos = particlePositions[start].toDoubleArray()
                val lastPos2 = particlePositions[start].toDoubleArray()

                var distCovered = 0.0

                val ySpeed = particlePositions[particlePositions.size - 1].xCoord - particlePositions[particlePositions.size - 2].xCoord / hypot(
                    particlePositions[particlePositions.size - 1].xCoord - particlePositions[particlePositions.size - 2].xCoord,
                    particlePositions[particlePositions.size - 1].zCoord - particlePositions[particlePositions.size - 2].xCoord
                )
                val estimatedBurrowDistance = estimatedBurrowDistance ?: return
                var i = start + 1
                while (distCovered < estimatedBurrowDistance && i < 10000) {
                    val y = b / (i + a) + c
                    val dist = distMultiplier * (0.06507 * i + 0.259) // this is where inaccuracy comes from

                    val xOff = dist * sin(y)
                    val zOff = dist * cos(y)

                    val density = 5

                    for (o in 0..density) {
                        lastPos[0] += xOff / density
                        lastPos[2] += zOff / density

                        lastPos[1] += ySpeed * dist / density
                        lastPos2[1] += ySpeed * dist / density

                        lastPos2[0] -= xOff / density
                        lastPos2[2] -= zOff / density

                        pr1.add(lastPos.toVec3())
                        pr2.add(lastPos2.toVec3())

                        lastSoundPoint?.let {
                            distCovered = hypot(lastPos[0] - it.xCoord, lastPos[2] - it.zCoord)
                        }

                        if (distCovered > estimatedBurrowDistance) break
                    }
                    i++
                }
                if (pr1.isEmpty()) return

                val p1 = pr1.last()
                val p2 = pr2.last()

                estimatedBurrowPosition?.let {
                    DianaHelper.renderPos = findNearestGrassBlock(
                        if (
                            ((p1.xCoord - it.xCoord) * (2 + p1.zCoord - it.zCoord)).pow(2) <
                            ((p2.xCoord - it.xCoord) * (2 + p2.zCoord - it.zCoord)).pow(2)
                        )
                            Vec3(floor(p1.xCoord), 120.0, floor(p1.zCoord))
                        else
                            Vec3(floor(p2.xCoord), 120.0, floor(p2.zCoord))
                    )
                }
            }
        }
    }

    fun reset() {
        lastDingTime = 0L
        lastDingPitch = 0f
        firstPitch = 0f
        lastParticlePosition = null
        secondLastParticlePosition = null
        lastSoundPoint = null
        firstParticlePoint = null
        currentParticlePosition = null
        estimatedBurrowPosition = null
        numberOfDings = 0
        dingPitchSlopes.clear()
        burrows.clear()
    }

    private enum class ParticleType(val check: S2APacketParticles.() -> Boolean) {
        EMPTY   ({ particleType == EnumParticleTypes.CRIT_MAGIC && particleCount == 4 && particleSpeed == .01f && xOffset == .5f && yOffset == .1f && zOffset == .5f }),
        MOB     ({ particleType == EnumParticleTypes.CRIT && particleCount == 3 && particleSpeed == .01f && xOffset == .5f && yOffset == .1f && zOffset == .5f }),
        TREASURE({ particleType == EnumParticleTypes.DRIP_LAVA && particleCount == 2 && particleSpeed == .01f && xOffset == .35f && yOffset == .1f && zOffset == .35f }),
        FOOTSTEP({ particleType == EnumParticleTypes.FOOTSTEP && particleCount == 1 && particleSpeed == .0f && xOffset == .05f && yOffset == .0f && zOffset == .05f }),
        ENCHANT ({ particleType == EnumParticleTypes.ENCHANTMENT_TABLE && particleCount == 5 && particleSpeed == .05f && xOffset == .5f && yOffset == .4f && zOffset == .5f });

        companion object {
            fun getParticleType(packet: S2APacketParticles): ParticleType? {
                if (!packet.isLongDistance) return null
                return entries.find { it.check(packet) }
            }
        }
    }

    class Burrow(
        var location: Vec3i,
        var hasFootstep: Boolean = false,
        var hasEnchant: Boolean = false,
        var type: Int = -1,
        var found: Boolean = false,
    ) {
        fun getType(): DianaHelper.BurrowType {
            return when (this.type) {
                0 -> DianaHelper.BurrowType.START
                1 -> DianaHelper.BurrowType.MOB
                2 -> DianaHelper.BurrowType.TREASURE
                else -> DianaHelper.BurrowType.UNKNOWN
            }
        }
    }
}
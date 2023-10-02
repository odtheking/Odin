package me.odinclient.utils.skyblock

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.impl.skyblock.DianaHelper
import me.odinclient.utils.VecUtils.clone
import me.odinclient.utils.VecUtils.coerceYIn
import me.odinclient.utils.VecUtils.pos
import me.odinclient.utils.VecUtils.solveEquationThing
import me.odinclient.utils.VecUtils.toDoubleArray
import me.odinclient.utils.VecUtils.toVec3
import me.odinclient.utils.skyblock.ItemUtils.isHolding
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import tv.twitch.chat.Chat
import kotlin.math.*

object DianaBurrowEstimate {

    private var dingIndex = 0
    private var lastDing = 0L
    private var lastDingPitch = 0f
    private var firstPitch = 0f
    private var lastParticlePoint: Vec3? = null
    private var lastParticlePoint2: Vec3? = null
    private var firstParticlePoint: Vec3? = null
    private var particlePoint: Vec3? = null
    private var guessPoint: Vec3? = null

    private var lastSoundPoint: Vec3? = null
    private var locs = mutableListOf<Vec3>()
    private val burrows = mutableMapOf<Vec3i, Burrow>()
    private var lastBurrow: Vec3i? = null
    val recentBurrows = mutableListOf<Vec3i>()

    private var dingSlope = mutableListOf<Float>()

    private var distance: Double? = null


    fun reset() {
        lastDing = 0L
        lastDingPitch = 0f
        firstPitch = 0f
        lastParticlePoint = null
        lastParticlePoint2 = null
        lastSoundPoint = null
        firstParticlePoint = null
        particlePoint = null
        guessPoint = null
        dingIndex = 0
        dingSlope.clear()
        burrows.clear()
    }

    private fun findNearestGrassBlock(pos: Vec3): Vec3 {
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(pos))
        if (!chunk.isLoaded) return pos.coerceYIn(50.0, 90.0)

        val blocks = List(70) { i -> BlockPos(pos.xCoord, i + 50.0, pos.zCoord) }.filter { chunk.getBlock(it) == Blocks.grass }
        if (blocks.isEmpty()) return pos.coerceYIn(50.0, 90.0)
        return Vec3(blocks.minBy { abs(pos.yCoord - it.y) })
    }

    fun handleSoundPacket(it: S29PacketSoundEffect) {
        if (it.soundName != "note.harp" || LocationUtils.currentArea != "Hub") return

        if (lastDing == 0L) firstPitch = it.pitch

        lastDing = System.currentTimeMillis()

        if (it.pitch < lastDingPitch) reset()

        if (lastDingPitch == 0f) {
            lastDingPitch = it.pitch
            lastParticlePoint = null
            lastParticlePoint2 = null
            lastSoundPoint = null
            firstParticlePoint = null
            locs.clear()
            return
        }

        dingIndex++
        if (dingIndex > 1) dingSlope.add(it.pitch - lastDingPitch)
        if (dingSlope.size > 20) dingSlope.removeFirst()
        val slope = if (dingSlope.isNotEmpty()) dingSlope.average() else 0.0
        lastSoundPoint = it.pos
        lastDingPitch = it.pitch

        if (lastParticlePoint2 == null || particlePoint == null || firstParticlePoint == null) return

        distance = (Math.E / slope) - firstParticlePoint?.distanceTo(it.pos)!!

        if (distance!! > 1000) {
            distance = null
            return
        }

        val lineDist = lastParticlePoint2?.distanceTo(particlePoint!!)!!

        val changesHelp = particlePoint?.subtract(lastParticlePoint2!!)!!
        val changes = listOf(changesHelp.xCoord, changesHelp.yCoord, changesHelp.zCoord).map { it / lineDist }

        lastSoundPoint?.let {
            guessPoint = Vec3(it.xCoord + changes[0] * distance!!, it.yCoord + changes[1] * distance!!, it.zCoord + changes[2] * distance!!)
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
        if (locs.size < 100 && locs.isEmpty() || locs.last().distanceTo(currLoc) != 0.0) {
            var distMultiplier = 1.0
            if (locs.size > 2) {
                val predictedDist = 0.06507 * locs.size + 0.259
                val lastPos = locs.last()
                val actualDist = currLoc.distanceTo(lastPos)
                distMultiplier = actualDist / predictedDist
            }
            locs.add(currLoc)

            if (locs.size > 5 && guessPoint != null) {
                val slopeThing = locs.zipWithNext { a, b -> atan((a.xCoord - b.xCoord) / (a.zCoord - b.zCoord)) }
                val (a, b, c) = solveEquationThing(
                    Vec3(slopeThing.size - 5.0, slopeThing.size - 3.0, slopeThing.size - 1.0),
                    Vec3(slopeThing[slopeThing.size - 5], slopeThing[slopeThing.size - 3], slopeThing[slopeThing.size - 1])
                )

                val pr1 = mutableListOf<Vec3>()
                val pr2 = mutableListOf<Vec3>()

                val start = slopeThing.size - 1
                val lastPos = locs[start].toDoubleArray()
                val lastPos2 = locs[start].toDoubleArray()

                var distCovered = 0.0

                val ySpeed = locs[locs.size - 1].xCoord - locs[locs.size - 2].xCoord / hypot(
                    locs[locs.size - 1].xCoord - locs[locs.size - 2].xCoord,
                    locs[locs.size - 1].zCoord - locs[locs.size - 2].xCoord
                )

                var i = start + 1
                while (distCovered < distance!! && i < 10000) {
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

                        if (distCovered > distance!!) break
                    }
                    i++
                }
                if (pr1.isEmpty()) return

                val p1 = pr1.last()
                val p2 = pr2.last()

                guessPoint?.let {
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

        if (lastParticlePoint == null) firstParticlePoint = currLoc.clone()
        lastParticlePoint2 = lastParticlePoint
        lastParticlePoint = particlePoint
        particlePoint = currLoc.clone()

        if (lastParticlePoint2 == null || firstParticlePoint == null || distance == null || lastSoundPoint == null) return
        val lineDist = lastParticlePoint2?.distanceTo(particlePoint!!)!!

        val changesHelp = particlePoint?.subtract(lastParticlePoint2!!)!!
        val changes = listOf(changesHelp.xCoord, changesHelp.yCoord, changesHelp.zCoord).map { it / lineDist }

        lastParticlePoint?.let {
            guessPoint = Vec3(it.xCoord + changes[0] * distance!!, it.yCoord + changes[1], it.zCoord + changes[2] * distance!!)
        }
    }

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

    fun blockEvent(pos: Vec3i) {
        if (pos !in burrows.keys || !isHolding("ANCESTRAL_SPADE")) return
        lastBurrow = pos
    }

    fun chat(event: ChatPacketEvent) {
        if (!event.message.startsWith("You dug out a Griffin Burrow!") && event.message != "You finished the Griffin burrow chain! (4/4)") return
        lastBurrow?.let {
            recentBurrows.add(it)
            burrows.remove(it)
            DianaHelper.burrowsRender.remove(it)
            lastBurrow = null
        }
    }
}

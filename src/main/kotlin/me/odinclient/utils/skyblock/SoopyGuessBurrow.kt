package me.odinclient.utils.skyblock

import me.odinclient.features.impl.skyblock.DianaHelper
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import kotlin.math.*


object SoopyGuessBurrow {

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

    private var dingSlope = mutableListOf<Float>()

    private var distance: Double? = null
    private var distance2: Double? = null

    private val S29PacketSoundEffect.pos: Vec3
        get() = Vec3(this.x, this.y, this.z)

    private fun reset() {
        lastDing = 0L
        lastDingPitch = 0f
        firstPitch = 0f
        lastParticlePoint = null
        lastParticlePoint2 = null
        lastSoundPoint = null
        firstParticlePoint = null
        particlePoint = null
        guessPoint = null
        distance = null
        dingIndex = 0
        dingSlope.clear()
    }

    fun onWorldLoad() {
        reset()
    }

    fun handleSoundPacket(it: S29PacketSoundEffect) {
        if (it.soundName != "note.harp") return
        val pitch = it.pitch

        if (lastDing == 0L) {
            firstPitch = pitch
        }

        lastDing = System.currentTimeMillis()

        if (pitch < lastDingPitch) {
            firstPitch = pitch
            dingIndex = 0
            dingSlope.clear()
            lastDingPitch = pitch
            lastParticlePoint = null
            lastParticlePoint2 = null
            lastSoundPoint = null
            firstParticlePoint = null
            distance = null
            locs.clear()
        }

        if (lastDingPitch == 0f) {
            lastDingPitch = pitch
            distance = null
            lastParticlePoint = null
            lastParticlePoint2 = null
            lastSoundPoint = null
            firstParticlePoint = null
            locs.clear()
            return
        }

        dingIndex++

        if (dingIndex > 1) dingSlope.add(pitch - lastDingPitch)
        if (dingSlope.size > 20) dingSlope.removeFirst()
        val slope =
            if (dingSlope.isNotEmpty()) dingSlope.reduce { a, b -> a + b }.toDouble() / dingSlope.size else 0.0
        val pos = it.pos
        lastSoundPoint = pos
        lastDingPitch = pitch

        if (lastParticlePoint2 == null || particlePoint == null || firstParticlePoint == null) {
            return
        }

        distance2 = (Math.E / slope) - firstParticlePoint?.distanceTo(pos)!!

        if (distance2!! > 1000) {
            ChatUtils.modMessage("distance too big: $distance2")
            distance2 = null
            return
        }

        val lineDist = lastParticlePoint2?.distanceTo(particlePoint!!)!!

        distance = distance2!!
        val changesHelp = particlePoint?.subtract(lastParticlePoint2!!)!!
        var changes = listOf(changesHelp.xCoord, changesHelp.yCoord, changesHelp.zCoord)
        changes = changes.map { o -> o / lineDist }

        lastSoundPoint?.let {
            guessPoint =
                Vec3(
                    it.xCoord + changes[0] * distance!!,
                    it.yCoord + changes[1] * distance!!,
                    it.zCoord + changes[2] * distance!!
                )
        }

    }


    private fun solveEquationThing(x: Vec3, y: Vec3): Triple<Double, Double, Double> {
        val a = (-y.xCoord * x.yCoord * x.xCoord - y.yCoord * x.yCoord * x.zCoord + y.yCoord * x.yCoord * x.xCoord + x.yCoord * x.zCoord * y.zCoord + x.xCoord * x.zCoord * y.xCoord - x.xCoord * x.zCoord * y.zCoord) / (x.yCoord * y.xCoord - x.yCoord * y.zCoord + x.xCoord * y.zCoord - y.xCoord * x.zCoord + y.yCoord * x.zCoord - y.yCoord * x.xCoord)
        val b = (y.xCoord - y.yCoord) * (x.xCoord + a) * (x.yCoord + a) / (x.yCoord - x.xCoord)
        val c = y.xCoord - b / (x.xCoord + a)
        return Triple(a, b, c)
    }

    fun handleParticlePacket(it: S2APacketParticles) {
        if (it.particleType != EnumParticleTypes.DRIP_LAVA) return
        val currLoc = Vec3(it.xCoordinate, it.yCoordinate, it.zCoordinate)

        var run = false
        lastSoundPoint?.let {
            if (abs(currLoc.xCoord - it.xCoord) < 2 && abs(currLoc.yCoord - it.yCoord) < 0.5 && abs(currLoc.zCoord - it.zCoord) < 2) {
                run = true
            }
        }
        if (run) {
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

                    val slopeThing = locs.zipWithNext { a, b ->
                        atan((a.xCoord - b.xCoord) / (a.zCoord - b.zCoord))
                    }

                    val (a, b, c) = solveEquationThing(
                        Vec3(
                            (slopeThing.size - 5).toDouble(),
                            (slopeThing.size - 3).toDouble(),
                            (slopeThing.size - 1).toDouble()
                        ),
                        Vec3(
                            slopeThing[slopeThing.size - 5],
                            slopeThing[slopeThing.size - 3],
                            slopeThing[slopeThing.size - 1]
                        )
                    )

                    val pr1 = mutableListOf<Vec3>()
                    val pr2 = mutableListOf<Vec3>()

                    val start = slopeThing.size - 1
                    val lastPos = locs[start].multiply(1.0).toDoubleArray()
                    val lastPos2 = locs[start].multiply(1.0).toDoubleArray()

                    var distCovered = 0.0

                    val ySpeed = locs[locs.size - 1].xCoord - locs[locs.size - 2].xCoord / hypot(
                        locs[locs.size - 1].xCoord - locs[locs.size - 2].xCoord,
                        locs[locs.size - 1].zCoord - locs[locs.size - 2].xCoord
                    )

                    var i = start + 1
                    while (distCovered < distance2!! && i < 10000) {
                        val y = b / (i + a) + c
                        val dist = distMultiplier * (0.06507 * i + 0.259)

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

                            if (distCovered > distance2!!) break
                        }
                        i++
                    }

                    //Why does this happen?
                    if (pr1.isEmpty()) return

                    val p1 = pr1.last()
                    val p2 = pr2.last()


                    guessPoint?.let {
                        val d1 = ((p1.xCoord - it.xCoord).times(2 + (p1.zCoord - it.zCoord))).pow(2)
                        val d2 = ((p2.xCoord - it.xCoord).times(2 + (p2.zCoord - it.zCoord))).pow(2)

                        val finalLocation = if (d1 < d2) {
                            Vec3(floor(p1.xCoord), 255.0, floor(p1.zCoord))
                        } else {
                            Vec3(floor(p2.xCoord), 255.0, floor(p2.zCoord))
                        }
                        DianaHelper.renderPos = finalLocation
                    }
                }
            }

            if (lastParticlePoint == null) {
                firstParticlePoint = currLoc.clone()
            }

            lastParticlePoint2 = lastParticlePoint
            lastParticlePoint = particlePoint

            particlePoint = currLoc.clone()

            if (lastParticlePoint2 == null || firstParticlePoint == null || distance2 == null || lastSoundPoint == null) return


            val lineDist = lastParticlePoint2?.distanceTo(particlePoint!!)!!

            distance = distance2!!

            val changesHelp = particlePoint?.subtract(lastParticlePoint2!!)!!

            var changes = listOf(changesHelp.xCoord, changesHelp.yCoord, changesHelp.zCoord)
            changes = changes.map { o -> o / lineDist }

            lastParticlePoint?.let {
                guessPoint =
                    Vec3(
                        it.xCoord + changes[0] * distance!!,
                        it.yCoord + changes[1],
                        it.zCoord + changes[2] * distance!!
                    )
            }
        }
    }

    private fun Vec3.clone(): Vec3 = Vec3(this.xCoord, this.yCoord, this.zCoord)

    private fun Vec3.toDoubleArray(): DoubleArray {
        return doubleArrayOf(xCoord, yCoord, zCoord)
    }

    private fun DoubleArray.toVec3(): Vec3 {
        return Vec3(this[0], this[1], this[2])
    }

    private fun Vec3.multiply(d: Double): Vec3 = Vec3(xCoord multiplyZeroSave d, yCoord multiplyZeroSave d, zCoord multiplyZeroSave d)

    private infix fun Double.multiplyZeroSave(other: Double): Double {
        val result = this * other
        return if (result == -0.0) 0.0 else result
    }
}
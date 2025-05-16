package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.calculateMean
import me.odinmain.utils.covarianceMatrix
import me.odinmain.utils.dot
import me.odinmain.utils.findMainEigenvector
import me.odinmain.utils.positionVector
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.scale
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.ui.Colors
import net.minecraft.entity.passive.EntitySheep
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.math.abs

object MageBeam : Module(
    name = "Mage Beam",
    desc = "Allows you to customize the rendering of the mage beam ability."
) {
    private val duration by NumberSetting("Duration", 1500, 100, 4000, 100, unit = "ms", desc = "The duration of the beam.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_BLUE, true, desc = "The color of the beam.")
    private val lineWidth by NumberSetting("Line Width", 2f, 1f, 10f, 0.1f, desc = "The width of the beam line.")
    private val depth by BooleanSetting("Depth Check", true, desc = "Whether or not to depth check the beam.")
    private val hideParticles by BooleanSetting("Hide Particles", true, desc = "Whether or not to hide the particles.")
    private val removeSheep by BooleanSetting("Remove Sheep", true, desc = "Removes sheep that spawn along the beam.")
    private val beamFade by BooleanSetting("Beam Fade", false, desc = "Gradually fades the beam")
    private val centerLineOnly by BooleanSetting("Center Line Only", true, desc = "Only shows the center line for sheep beam.")
    private val particleCollectionTime by NumberSetting("Particle Collection Time", 8, 4, 27, 1, unit = "ms",
        desc = "Time window to collect particles before processing.")

    private data class MageBeam(
        val points: CopyOnWriteArrayList<Vec3> = CopyOnWriteArrayList(),
        val expiryTime: Long,
        val isCenterLine: Boolean = false
    )

    private val activeBeams = CopyOnWriteArrayList<MageBeam>()
    private val currentParticles = CopyOnWriteArrayList<Vec3>()
    private val processedParticles = CopyOnWriteArraySet<Vec3>()

    private var firstParticleTime: Long = -1L

    private val processingLock = Object()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        val currentTime = System.currentTimeMillis()

        activeBeams.removeIf { it.expiryTime <= currentTime }

        if (firstParticleTime != -1L &&
            currentParticles.isNotEmpty() &&
            currentTime - firstParticleTime >= particleCollectionTime) {
            synchronized(processingLock) {
                processCollectedParticles()
            }
        }
    }

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        if (!DungeonUtils.inDungeons || this !is S2APacketParticles || particleType != EnumParticleTypes.FIREWORKS_SPARK) return

        val newPoint = positionVector

        var alreadyProcessed = false
        synchronized(processingLock) {
            alreadyProcessed = isAlreadyProcessed(newPoint)
        }

        if (alreadyProcessed) {
            if (hideParticles) {
                event.isCanceled = true
            }
            return
        }

        synchronized(processingLock) {
            if (firstParticleTime == -1L) {
                firstParticleTime = System.currentTimeMillis()
            }

            currentParticles.add(newPoint)
        }

        if (hideParticles) {
            event.isCanceled = true
        }
    }

    private fun isAlreadyProcessed(point: Vec3): Boolean {
        val THRESHOLD = 0.01 * 0.01

        for (existingPoint in processedParticles) {
            if (point.squareDistanceTo(existingPoint) < THRESHOLD) {
                return true
            }
        }
        return false
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (!DungeonUtils.inDungeons || !removeSheep) return

        if (event.entity is EntitySheep) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || activeBeams.isEmpty()) return

        activeBeams.forEach { beam ->
            if (centerLineOnly && !beam.isCenterLine) return@forEach

            if (beam.points.size < 2) return@forEach
            val currentTime = System.currentTimeMillis()

            val alpha = if (beamFade) {
                val fadeProgress = ((beam.expiryTime - currentTime).toFloat() / duration).coerceIn(0f, 1f)
                color.alphaFloat * if (fadeProgress > 0.75f) 1f else fadeProgress / 0.75f
            } else 1f

            Renderer.draw3DLine(beam.points, Color(color.rgba, alpha), lineWidth, depth)
        }
    }

    private fun processCollectedParticles() {
        if (currentParticles.size < 6) {
            currentParticles.clear()
            firstParticleTime = -1L
            return
        }

        val points = ArrayList<Vec3>(currentParticles)

        // find cylinder axis direction using manual PCA
        // https://www.geeksforgeeks.org/principal-component-analysis-pca/
        val center = calculateMean(points)
        val covMatrix = covarianceMatrix(points, center)
        val mainAxis = findMainEigenvector(covMatrix)

        val assignedPoints = HashSet<Int>()
        val beamLines = ArrayList<Pair<List<Vec3>, Double>>()

        val currentTime = System.currentTimeMillis()

        val radialDistances = ArrayList<Pair<Int, Double>>(points.size)
        // calculate radial distance
        for (i in points.indices) {
            val point = points[i]
            val v = point.subtract(center)
            val projectionOnAxis = mainAxis.scale(v.dot(mainAxis))
            val radialComponent = v.subtract(projectionOnAxis)
            radialDistances.add(Pair(i, radialComponent.lengthVector()))
        }

        radialDistances.sortBy { it.second }

        // form beams
        for ((idx, radialDistance) in radialDistances) {
            if (idx in assignedPoints) continue

            // start new beam
            val currentBeam = ArrayList<Vec3>()
            currentBeam.add(points[idx])
            assignedPoints.add(idx)

            val beamDirection = mainAxis

            // find closest unassigned points along beams
            while (true) {
                var bestDistance = Double.POSITIVE_INFINITY
                var bestIdx = -1

                val currentPoint = currentBeam.last()

                // next point along the beams
                for (i in points.indices) {
                    if (i in assignedPoints) continue

                    val point = points[i]

                    val vector = point.subtract(currentPoint)

                    // project vector onto beam direction
                    val projection = vector.dot(beamDirection)

                    // calculate perpendicular distance from center of beam
                    val perpendicular = vector.subtract(beamDirection.scale(projection))
                    val perpDistance = perpendicular.lengthVector()

                    // calculate distance along the beam
                    val alongDistance = abs(projection)

                    if (perpDistance < 0.15) {
                        //prioritise distance to smallest
                        if (alongDistance < bestDistance) {
                            bestDistance = alongDistance
                            bestIdx = i
                        }
                    }
                }

                if (bestIdx == -1) break

                currentBeam.add(points[bestIdx])
                assignedPoints.add(bestIdx)
            }

            if (currentBeam.size >= 2) {
                val projections = ArrayList<Double>(currentBeam.size)

                for (p in currentBeam) {
                    val v = p.subtract(center)
                    projections.add(v.dot(beamDirection))
                }

                val sortedPoints = currentBeam.zip(projections)
                    .sortedBy { it.second }
                    .map { it.first }

                beamLines.add(Pair(sortedPoints, radialDistance))
            }
        }

        // sort beams by radial for center line
        beamLines.sortBy { it.second }

        // add the detected beam to active beams and mark particles as processed
        if (beamLines.isNotEmpty()) {
            // first beam is center

            beamLines.forEachIndexed { index, (linePoints, _) ->
                activeBeams.add(
                    MageBeam(
                        points = CopyOnWriteArrayList(linePoints),
                        expiryTime = currentTime + duration,
                        isCenterLine = index == 0
                    )
                )

                processedParticles.addAll(linePoints)
            }
        }

        currentParticles.clear()
        firstParticleTime = -1L
    }
}

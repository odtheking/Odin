package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.ParticleAddEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CopyOnWriteArrayList

object MageBeam : Module(
    name = "Mage Beam",
    description = "Allows you to customize the rendering of the mage beam ability."
) {
    private val duration by NumberSetting("Duration", 40, 1, 100, 1, unit = "ticks", desc = "The duration of the beam in ticks.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_RED, true, desc = "The color of the beam.")
    private val depth by BooleanSetting("Depth Check", true, desc = "Whether or not to depth check the beam.")
    private val hideParticles by BooleanSetting("Hide Particles", true, desc = "Whether or not to hide the particles.")
    private val minPoints by NumberSetting("Min Points", 3, 2, 10, 1, desc = "Minimum number of points required to render a beam.")

    private data class MageBeamData(
        val points: CopyOnWriteArrayList<Vec3> = CopyOnWriteArrayList(),
        var lastUpdateTick: Int = 0,
        var closestPoint: Vec3? = null,
        var furthestPoint: Vec3? = null
    ) {
        fun updateEndpoints(playerPos: Vec3) {
            val pointsList = points.toList()
            if (pointsList.isEmpty()) return

            var closest = pointsList[0]
            var furthest = pointsList[0]
            var minDistSqr = closest.distanceToSqr(playerPos)
            var maxDistSqr = minDistSqr

            for (i in 1 until pointsList.size) {
                val point = pointsList[i]
                val distSqr = point.distanceToSqr(playerPos)

                if (distSqr < minDistSqr) {
                    minDistSqr = distSqr
                    closest = point
                }
                if (distSqr > maxDistSqr) {
                    maxDistSqr = distSqr
                    furthest = point
                }
            }

            closestPoint = closest
            furthestPoint = furthest
        }
    }

    private val activeBeams = CopyOnWriteArrayList<MageBeamData>()
    private var currentTick = 0

    init {
        on<ParticleAddEvent> {
            if (!DungeonUtils.inDungeons || particle != ParticleTypes.FIREWORK) return@on

            val recentBeam = activeBeams.lastOrNull()

            if (recentBeam != null && (currentTick - recentBeam.lastUpdateTick) < 1 && isPointInBeamDirection(recentBeam.points, pos)) {
                recentBeam.points.add(pos)
                recentBeam.lastUpdateTick = currentTick
            } else {
                val newBeam = MageBeamData(CopyOnWriteArrayList<Vec3>().apply { add(pos) }, currentTick)
                activeBeams.add(newBeam)
                schedule(duration, true) {
                    activeBeams.remove(newBeam)
                }
            }

            if (hideParticles) cancel()
        }

        on<TickEvent.Server> {
            if (!DungeonUtils.inDungeons) return@on
            currentTick++

            val playerPos = mc.player?.position() ?: return@on
            for (beam in activeBeams) {
                beam.updateEndpoints(playerPos)
            }
        }

        on<RenderEvent.Extract> {
            if (!DungeonUtils.inDungeons) return@on

            for (beam in activeBeams) {
                val pointsSize = beam.points.size
                if (pointsSize < minPoints) continue

                val closest = beam.closestPoint ?: continue
                val furthest = beam.furthestPoint ?: continue
                if (closest == furthest) continue

                drawLine(listOf(closest, furthest), color, depth, 8f)
            }
        }

        on<WorldEvent.Load> {
            activeBeams.clear()
            currentTick = 0
        }
    }

    private fun isPointInBeamDirection(points: List<Vec3>, newPoint: Vec3): Boolean {
        if (points.size <= 1) return true

        val lastPoint = points.last()
        return lastPoint.subtract(points[0]).normalize().dot(newPoint.subtract(lastPoint).normalize()) > 0.99
    }
}
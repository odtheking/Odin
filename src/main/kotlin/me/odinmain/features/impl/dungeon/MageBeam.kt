package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.positionVector
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList

object MageBeam : Module(
    name = "Mage Beam",
    desc = "Allows you to customize the rendering of the mage beam ability."
) {
    private val duration by NumberSetting("Duration", 40, 1, 100, 1, unit = "ticks", desc = "The duration of the beam in ticks.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_RED, true, desc = "The color of the beam.")
    private val lineWidth by NumberSetting("Line Width", 2f, 1f, 10f, 0.1f, desc = "The width of the beam line.")
    private val depth by BooleanSetting("Depth Check", true, desc = "Whether or not to depth check the beam.")
    private val hideParticles by BooleanSetting("Hide Particles", true, desc = "Whether or not to hide the particles.")

    private data class MageBeam(
        val points: CopyOnWriteArrayList<Vec3> = CopyOnWriteArrayList(),
        var lastUpdateTick: Int,
        val expiryTick: Int = 0
    )

    private val activeBeams = CopyOnWriteArrayList<MageBeam>()
    private var currentTick = 0

    const val MAX_PARTICLE_LINK_DISTANCE = 0.9
    const val MAX_PARTICLE_LINK_DISTANCE_SQ = MAX_PARTICLE_LINK_DISTANCE * MAX_PARTICLE_LINK_DISTANCE

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null) return

        currentTick++

        // cleanup after dungeon
        if (!DungeonUtils.inDungeons) {
            if (activeBeams.isNotEmpty()) {
                activeBeams.clear()
            }
            return
        }
        activeBeams.removeIf { beam -> currentTick >= beam.expiryTick }
    }

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        if (!DungeonUtils.inDungeons || this !is S2APacketParticles || particleType != EnumParticleTypes.FIREWORKS_SPARK) return

        val newPoint = positionVector

        // trye extend old beam
        val beamToExtend = activeBeams.reversed().find { beam ->
            if (beam.points.isEmpty()) return@find false

            // 1 tick gap between particles
            if ((currentTick - beam.lastUpdateTick) > 1) return@find false

            // close to last part of beam
            val lastPoint = beam.points.last()
            if (newPoint.squareDistanceTo(lastPoint) > MAX_PARTICLE_LINK_DISTANCE_SQ) return@find false

            // try to continue from base direction
            isPointInBeamDirection(beam.points, newPoint)
        }

        if (beamToExtend != null) {
            beamToExtend.points.add(newPoint)
            beamToExtend.lastUpdateTick = currentTick
        } else {
            val newBeam = MageBeam(
                points = CopyOnWriteArrayList<Vec3>().apply { add(newPoint) },
                lastUpdateTick = currentTick,
                expiryTick = currentTick + duration
            )
            activeBeams.add(newBeam)
        }

        if (hideParticles) {
            event.isCanceled = true
        }
    }

    private fun isPointInBeamDirection(points: List<Vec3>, newPoint: Vec3): Boolean {
        if (points.size <= 2) return true

        val firstPoint = points[0]
        val lastPoint = points.last()

        // for short beams
        val totalBeamLengthSq = lastPoint.squareDistanceTo(firstPoint)
        if (totalBeamLengthSq < 1e-12) {
            val pointBeforeLast = points[points.size - 2]

            if (lastPoint.squareDistanceTo(pointBeforeLast) < 1e-12) return true

            val lastSegmentVector = lastPoint.subtract(pointBeforeLast).normalize()
            val newSegmentVector  = newPoint.subtract(lastPoint).normalize()
            // approx 26 degrees.
            return lastSegmentVector.dotProduct(newSegmentVector) > 0.90
        }

        // overall direction
        val totalBeamVector = lastPoint.subtract(firstPoint).normalize()
        val newSegmentVector  = newPoint.subtract(lastPoint).normalize()
        // approx 8 degrees.
        return totalBeamVector.dotProduct(newSegmentVector) > 0.99
    }

    // try reduce amount of zig zag by indexing last 2 points
    private fun isCompatibleWithBeam(beam: MageBeam, newPoint: Vec3): Boolean {
        val points = beam.points
        if (points.size < 3) return true

        val vec1 = points[points.size - 2].subtract(points[points.size - 3]).normalize()
        val vec2 = points.last().subtract(points[points.size - 2]).normalize()
        val newVec = newPoint.subtract(points.last()).normalize()

        return vec1.dotProduct(vec2) > 0.95 && vec2.dotProduct(newVec) > 0.95
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || activeBeams.isEmpty()) return

        activeBeams.forEach { beam ->
            if (beam.points.size < 2) return@forEach
            Renderer.draw3DLine(beam.points, color, lineWidth, depth)
        }
    }
}
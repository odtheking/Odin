package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.positionVector
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

object MageBeam: Module (
    name = "Mage Beam",
    desc = "Allows you to customize the rendering of the mage beam ability."
) {
    private val duration by NumberSetting("Duration", 40, 1, 100, 1, unit = "ticks", desc = "The duration of the beam in ticks.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_DARK_RED, true, desc = "The color of the beam.")
    private val lineWidth by NumberSetting("Line Width", 2f, 1f, 10f, 0.1f, desc = "The width of the beam line.")
    private val depth by BooleanSetting("Depth Check", true, desc = "Whether or not to depth check the beam.")
    private val hideParticles by BooleanSetting("Hide Particles", true, desc = "Whether or not to hide the particles.")

    private data class MageBeam(val points: CopyOnWriteArrayList<Vec3> = CopyOnWriteArrayList(), var lastUpdateTick: Int = 0)

    private val activeBeams = CopyOnWriteArrayList<MageBeam>()
    private var currentTick = 0

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) = with(event.packet) {
        if (!DungeonUtils.inDungeons || this !is S2APacketParticles || particleType != EnumParticleTypes.FIREWORKS_SPARK) return

        val recentBeam = activeBeams.lastOrNull()
        val newPoint = positionVector

        if (recentBeam != null && (currentTick - recentBeam.lastUpdateTick) < 1 && isPointInBeamDirection(recentBeam.points, newPoint)) {
            recentBeam.points.add(newPoint)
            recentBeam.lastUpdateTick = currentTick
        } else {
            val newBeam = MageBeam(CopyOnWriteArrayList<Vec3>().apply { add(newPoint) }, currentTick)
            activeBeams.add(newBeam)
            runIn(duration, true) {
                activeBeams.remove(newBeam)
            }
        }

        if (hideParticles) event.isCanceled = true
    }

    private fun isPointInBeamDirection(points: List<Vec3>, newPoint: Vec3): Boolean {
        if (points.size <= 1) return true

        val lastPoint = points.last()

        return lastPoint.subtract(points[0]).normalize().dotProduct(newPoint.subtract(lastPoint).normalize()) > 0.99
    }

//    @SubscribeEvent
//    fun onServerTick(event: ServerTickEvent) {
//        currentTick++
//    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons) return
        for (beam in activeBeams) {
            if (beam.points.size < 8) continue
            Renderer.draw3DLine(beam.points, color, lineWidth, depth)
        }
    }
}

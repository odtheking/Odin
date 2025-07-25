package me.odinmain.features.impl.render

import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

object WaypointManager {

    private var temporaryWaypoints = mutableListOf<Pair<Waypoint, Clock>>()

    fun addTempWaypoint(name: String = "§fWaypoint", x: Int, y: Int, z: Int, time: Long = 60_000) {
        if (!Waypoints.enabled) return
        if (listOf(x, y,z).any { abs(it) > 5000}) return modMessage("§cWaypoint out of bounds.")
        if (temporaryWaypoints.any { it.first.x == x && it.first.y == y && it.first.z == z }) return modMessage("§cWaypoint already exists at $x, $y, $z.")
        modMessage("§aAdded temporary waypoint at §6$x§r, §3$y§r, §d$z§r.")
        temporaryWaypoints.add(Pair(Waypoint(name, x, y, z, colors.random()), Clock(time)))
    }

    private val colors = listOf(
        Colors.MINECRAFT_GOLD, Colors.MINECRAFT_GREEN, Colors.MINECRAFT_LIGHT_PURPLE, Colors.MINECRAFT_DARK_AQUA, Colors.MINECRAFT_YELLOW, Colors.MINECRAFT_DARK_RED, Colors.WHITE,
        Colors.MINECRAFT_DARK_PURPLE, Colors.MINECRAFT_YELLOW, Colors.MINECRAFT_RED, Colors.MINECRAFT_LIGHT_PURPLE, Colors.MINECRAFT_DARK_GREEN, Colors.MINECRAFT_BLUE
    )

    fun addTempWaypoint(name: String = "§fWaypoint", vec3: Vec3i) {
        addTempWaypoint(name, vec3.x, vec3.y, vec3.z)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!Waypoints.enabled) return
        temporaryWaypoints.removeAll {
            it.first.renderBeacon()
            it.second.hasTimePassed()
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        temporaryWaypoints.clear()
    }

    data class Waypoint(
        var name: String,
        var x: Int,
        var y: Int,
        var z: Int,
        var color: Color,
        var shouldShow: Boolean = true,
    ) {
        fun renderBeacon() = Renderer.drawCustomBeacon(name, Vec3(x.toDouble(), y.toDouble(), z.toDouble()), color)
    }
}

package me.odinmain.features.impl.render

import me.odinmain.config.WaypointConfig
import me.odinmain.ui.waypoint.WaypointGUI
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils.currentArea
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.math.abs

// TODO: Make changes cuz ngl its kinda eh (eg: good ordered waypoints for mining so people dont need to use ct)
// TODO: Make all waypoint areas constant and make LocationUtils use locraw
// this is o
object WaypointManager {

    private inline val waypoints get() = WaypointConfig.waypoints
    private var temporaryWaypoints = mutableListOf<Pair<Waypoint, Clock>>()

    fun addWaypoint(name: String = "§fWaypoint", x: Int, y: Int, z: Int, color: Color = randomColor()) =
        addWaypoint(Waypoint(name, x, y, z, color))

    fun addWaypoint(name: String = "§fWaypoint", vec3: Vec3i, color: Color = randomColor()) =
        addWaypoint(Waypoint(name, vec3.x, vec3.y, vec3.z, color))

    fun addWaypoint(waypoint: Waypoint, area: String = currentArea.displayName) {
        waypoints.getOrPut(area) { mutableListOf() }.add(waypoint)
        WaypointConfig.saveConfig()
    }

    fun removeWaypoint(name: String) {
        val matchingWaypoint = waypoints[currentArea.displayName]?.find { it.name.noControlCodes.lowercase() == name } ?: return
        removeWaypoint(matchingWaypoint)
    }

    fun removeWaypoint(waypoint: Waypoint) {
        waypoints[WaypointGUI.displayArea]?.remove(waypoint)
        WaypointConfig.saveConfig()
    }

    fun clearWaypoints() {
        waypoints[currentArea.displayName]?.clear()
        WaypointConfig.saveConfig()
    }

    fun addTempWaypoint(name: String = "§fWaypoint", x: Int, y: Int, z: Int, time: Long = 60_000) {
        if (currentArea.isArea(Island.Unknown)) return modMessage("§cYou are not in Skyblock.")
        if (!Waypoints.enabled) return
        if (listOf(x, y,z).any { abs(it) > 5000}) return modMessage("§cWaypoint out of bounds.")
        if (temporaryWaypoints.any { it.first.x == x && it.first.y == y && it.first.z == z }) return modMessage("§cWaypoint already exists at $x, $y, $z.")
        modMessage("§aAdded temporary waypoint at §6$x§r, §3$y§r, §d$z§r.")
        temporaryWaypoints.add(Pair(Waypoint(name, x, y, z, colors.random()), Clock(time)))
    }

    private val colors = listOf(
        Color.ORANGE, Color.GREEN, Color.PINK, Color.CYAN, Color.YELLOW, Color.DARK_RED, Color.WHITE, Color.PURPLE, Color.YELLOW, Color.RED, Color.PINK
    )

    fun addTempWaypoint(name: String = "§fWaypoint", vec3: Vec3i) {
        addTempWaypoint(name, vec3.x, vec3.y, vec3.z)
    }

    fun randomColor(): Color {
        val random = Random()

        val hue = random.nextFloat()
        val saturation = random.nextFloat() * 0.5f + 0.5f // High saturation
        val brightness = random.nextFloat() * 0.5f + 0.5f // High brightness

        return Color(hue, saturation, brightness)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!Waypoints.enabled || currentArea.isArea(Island.Unknown)) return
        temporaryWaypoints.removeAll {
            it.first.renderBeacon()
            it.second.hasTimePassed()
        }

        waypoints[currentArea.displayName]?.forEach {
            if (it.shouldShow) it.renderBeacon()
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        temporaryWaypoints.clear()
        runIn(80) {
            if (!currentArea.isArea(Island.Unknown)) WaypointGUI.updateElements(currentArea.displayName)
        }
    }

    data class Waypoint(
        var name: String,
        var x: Int,
        var y: Int,
        var z: Int,
        var color: Color,
        var shouldShow: Boolean = true,
    ) {
        constructor(name: String, vec3: Vec3i, color: Color) : this(name, vec3.x, vec3.y, vec3.z, color, true)

        fun renderBeacon() = Renderer.drawCustomBeacon(name, Vec3(x.toDouble(), y.toDouble(), z.toDouble()), color)
    }
}

package me.odinclient.features.impl.render

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.commands.impl.WaypointCommand.randomColor
import me.odinclient.config.WaypointConfig
import me.odinclient.ui.waypoint.WaypointGUI
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.clock.Clock
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.LocationUtils.currentArea
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// TODO: Make changes cuz ngl its kinda eh (eg: good ordered waypoints for mining so people dont need to use ct)
// TODO: Make all waypoint areas constant and make LocationUtils use locraw
// this is o
object WaypointManager {

    private inline val waypoints get() = WaypointConfig.waypoints
    private var temporaryWaypoints = mutableListOf<Pair<Waypoint, Clock>>()

    fun addWaypoint(name: String = "§fWaypoint", x: Int, y: Int, z: Int, color: Color) =
        addWaypoint(Waypoint(name, x, y, z, color))

    fun addWaypoint(name: String = "§fWaypoint", vec3: Vec3i, color: Color) =
        addWaypoint(Waypoint(name, vec3.x, vec3.y, vec3.z, color))

    fun addWaypoint(waypoint: Waypoint,area: String = currentArea!!) {
        waypoints.getOrPut(area) { mutableListOf() }.add(waypoint)
        WaypointConfig.saveConfig()
    }

    fun removeWaypoint(name: String) {
        val matchingWaypoint = waypoints[currentArea]?.find { it.name.noControlCodes.lowercase() == name } ?: return
        removeWaypoint(matchingWaypoint)
    }

    fun removeWaypoint(waypoint: Waypoint) {
        waypoints[currentArea]?.remove(waypoint)
        WaypointConfig.saveConfig()
    }

    fun clearWaypoints() {
        waypoints[currentArea]?.clear()
        WaypointConfig.saveConfig()
    }

    fun addTempWaypoint(name: String = "§fWaypoint", x: Int, y: Int, z: Int) {
        if (currentArea == null) return modMessage("You are not in Skyblock.")
        temporaryWaypoints.add(Pair(Waypoint(name, x, y, z, randomColor()), Clock(60_000)))
    }

    fun addTempWaypoint(name: String = "§fWaypoint", vec3: Vec3i) {
        if (currentArea == null) return modMessage("You are not in Skyblock.")
        temporaryWaypoints.add(Pair(Waypoint(name, vec3.x, vec3.y, vec3.z, randomColor()), Clock(60_000)))
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!Waypoints.enabled || currentArea == null) return

        temporaryWaypoints.removeAll {
            if (!it.second.hasTimePassed()) {
                it.first.renderBeacon(event.partialTicks)
                false
            } else true
        }

        waypoints[currentArea]?.forEach {
            if (it.shouldShow) it.renderBeacon(event.partialTicks)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        temporaryWaypoints.clear()
        GlobalScope.launch {
            delay(4000)
            if (currentArea != null) WaypointGUI.updateElements(currentArea!!)
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

        fun renderBeacon(partialTicks: Float) = RenderUtils.renderCustomBeacon(name, x + .5, y + .5, z + .5, color, partialTicks)
    }
}
package me.odinclient.features.m7

import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import me.odinclient.OdinClient.Companion.config
import me.odinclient.utils.render.RenderUtils

object P5Waypoints {
// TODO make this into the waypoint system
    data class DungeonLocation(val x: Double, val y: Double, val z: Double, val label: String)

    private val locations = setOf(
        DungeonLocation(37.0, 15.0, 44.0, "decoy"),
        DungeonLocation(90.0, 12.0, 100.0, "decoy"),
        DungeonLocation(28.0, 6.0, 50.0, "gyro"),
        DungeonLocation(21.0, 12.0, 88.0, "decoy"),
        DungeonLocation(34.0, 6.0, 46.0, "gyro"),
        DungeonLocation(21.0, 12.0, 53.0, "decoy"),
        DungeonLocation(25.0, 6.0, 83.0, "gyro"),
        DungeonLocation(85.0, 6.0, 101.0, "gyro"),
        DungeonLocation(27.0, 16.0,94.0, "green"),
        DungeonLocation(23.0, 21.0, 54.0, "red"),
        DungeonLocation(84.0, 20.0, 59.0, "orange"),
        DungeonLocation(85.0, 20.0, 98.0, "blue"),
        DungeonLocation(56.0, 20.0, 124.0, "purple")
    )

    @SubscribeEvent
    fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
        if (!config.p5Waypoint/* || DungeonUtils.getPhase() != 5*/) return
        for (location in locations) {
            RenderUtils.renderBoxText("§f${location.label}", location.x, location.y, location.z, 200, 145, 234)
        }
    }
}
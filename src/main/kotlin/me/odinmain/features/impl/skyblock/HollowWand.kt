package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.sendCommand
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HollowWand : Module(
    name = "Hollow Wand",
    description = "Messages and waypoints for hollow wand casting."
) {
    private val sendChatMessage by BooleanSetting("Send Message", false, "Sends a message when hollow wand is casted.")
    private val showWaypoint by BooleanSetting("Show Waypoint", false, "Shows a waypoint when party hollow wand message is detected.")
    private val waypointColor by ColorSetting("Waypoint Color", Colors.MINECRAFT_DARK_RED, desc = "Color of the pool waypoint.").withDependency { showWaypoint }
    private val messageSize by NumberSetting("Message Size", 1f, 0.1f, 4f, 0.1f, desc = "The size of the waypoint text.").withDependency { showWaypoint }

    data class CastCoordinate(val x: Double, val y: Double, val z: Double, var radius: Double = 0.0)
    private val poolsToRender = mutableListOf<CastCoordinate>()
    private val windsToRender = mutableListOf<CastCoordinate>()

    private val incomingHollowRegex = Regex("^Party > (?:\\[[\\wዞ]+\\+*] )?[\\w_]+: (Raging Wind|Ichor Pool) x: (-?\\d+\\.\\d{3}), y: (-?\\d+), z: (-?\\d+\\.\\d{3})$")
    private val outgoingHollowRegex = Regex("^Casting Spell: (Raging Wind|Ichor Pool)!$")

    init {
        onMessage(outgoingHollowRegex) {
            val type = it.groups[1]?.value ?: return@onMessage
            if(sendChatMessage) sendCommand("pc $type ${PlayerUtils.getPositionString(true)}")
        }

        onMessage(incomingHollowRegex) {
            val type = it.groups[1]?.value ?: return@onMessage
            val x = it.groups[2]?.value?.toDouble() ?: return@onMessage
            val y = it.groups[3]?.value?.toDouble() ?: return@onMessage
            val z = it.groups[4]?.value?.toDouble() ?: return@onMessage
            val thisCast = CastCoordinate(x, y, z)
            when(type) {
                "Raging Wind" -> {
                    windsToRender.add(thisCast)
                }
                "Ichor Pool" -> {
                    poolsToRender.add(thisCast)
                    runIn(400, true) {
                        poolsToRender.remove(thisCast)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if(!showWaypoint) return

        poolsToRender.forEach { pool ->
            Renderer.drawCylinder(Vec3(pool.x, pool.y, pool.z), 8, 8, 0.05, 80f, 1f, 0f, 90f, 90f, waypointColor, true)
            Renderer.drawStringInWorld("Ichor Pool", Vec3(pool.x, pool.y + 0.5, pool.z), Colors.WHITE, true, 0.03f * messageSize)
        }

        windsToRender.forEach { wind ->
            wind.radius += 0.15
            if(wind.radius > 25) {
                windsToRender.remove(wind)
                return@forEach
            }

            Renderer.drawCylinder(Vec3(wind.x, wind.y, wind.z), wind.radius, wind.radius, 0.05, 80f, 1f, 0f, 90f, 90f, waypointColor, true)
            Renderer.drawStringInWorld("Raging Wind", Vec3(wind.x, wind.y + 0.5, wind.z), Colors.WHITE, true, 0.03f * messageSize)

        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        poolsToRender.clear()
    }
}

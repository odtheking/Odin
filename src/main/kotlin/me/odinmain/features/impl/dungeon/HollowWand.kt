package me.odinmain.features.impl.dungeon

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Color
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
    description = "Messages and waypoints for ichor pool casting"
) {
    private val sendChatMessage by BooleanSetting("Send Message", false, "Sends a message when ichor pool is casted.")
    private val showWaypoint by BooleanSetting("Show Waypoint", false,"Sends a waypoint when party ichor pool message is detected.")
    private val waypointColor by ColorSetting("Waypoint Color", Color(50, 150, 220), allowAlpha = false, desc = "Color of the ichor pool waypoint.").withDependency { showWaypoint }

    data class IchorPoolCoordinate(val x: Double, val y: Double, val z: Double)
    private val poolsToRender = mutableListOf<IchorPoolCoordinate>()

    private val incomingIchorRegex = Regex("""^Party > (?:\[[\wዞ]+\+*] )?[\w_]+: Ichor Pool x: (-?\d+), y: (-?\d+), z: (-?\d+)""")
    private val outgoingIchorRegex = Regex("""^Casting Spell: Ichor Pool!$""")
    private val messageSize by NumberSetting("Message Size", 1f, 0.1f, 4f, 0.1f, desc = "Whether or not to display the message size in the box.").withDependency { true }

    init {
        // Sending Ichor Pool Messages
        onMessage(outgoingIchorRegex) {
            if(!sendChatMessage) return@onMessage
            sendCommand("pc Ichor Pool ${PlayerUtils.getPositionString()}")
        }

        // Receiving Ichor Pool Messages
        onMessage(incomingIchorRegex) {
            val x: Double = it.groups[1]?.value?.toDouble() ?: return@onMessage
            val y: Double = it.groups[2]?.value?.toDouble() ?: return@onMessage
            val z: Double = it.groups[3]?.value?.toDouble() ?: return@onMessage
            poolsToRender.add(IchorPoolCoordinate(x, y, z))

            runIn(400, true) {
                poolsToRender.removeAt(0)
            }
        }
    }

    // Waypoint rendering and management

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if(!showWaypoint) return

        poolsToRender.forEach { pool ->
            Renderer.drawCylinder(Vec3(pool.x, pool.y, pool.z), 6, 6, 0.05, 80f, 1f, 0f, 90f, 90f, waypointColor, true)
            Renderer.drawStringInWorld("Ichor Pool", Vec3(pool.x, pool.y + 0.5, pool.z), Colors.WHITE, true, 0.03f * messageSize)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        poolsToRender.clear()
    }
}

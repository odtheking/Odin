package me.odinclient.features.general

import kotlinx.coroutines.*
import me.odinclient.OdinClient.Companion.config
import me.odinclient.utils.render.RenderUtils
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.random.Random
// TODO REMOVE THIS
object Waypoints {


    private val colors = listOf(
        Color(37, 245, 252),
        Color(242, 180, 36),
        Color(124, 252, 0),
        Color(0, 100, 0),
        Color(232, 28, 167)
    )

    private var waypoints = mutableListOf<Waypoint>()
    private data class Waypoint(val ign: String, val x: Double, val y: Double, val z: Double, val r: Int, val g: Int, val b: Int)

    @OptIn(DelicateCoroutinesApi::class)
    fun sixtySecondBeacon(ign: String, xPos: Double, yPos: Double, zPos: Double) {
        if (!config.waypoints) return
        val color = colors[Random.nextInt(colors.size)]
        waypoints.add(
            Waypoint(
                ign,
                xPos + 0.5,
                yPos,
                zPos + 0.5,
                color.red,
                color.green,
                color.blue
            )
        )
        GlobalScope.launch {
            delay(60000L)
            waypoints.removeAt(0)
        }
    }

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        if (!config.waypoints ) return
        val message = StringUtils.stripControlCodes(event.message.unformattedText)
        val matchResult =
            Regex("Party > (\\[.+\\])? (.{0,16}): Vanquisher spawned at: x: (-?\\d+) y: (-?\\d+) z: (-?\\d+)").find(
                message
            )
        val rank = matchResult?.groups?.get(1)?.value ?: return
        val player = matchResult.groups[2]?.value ?: return
        val x = matchResult.groups[3]?.value?.toDouble() ?: return
        val y = matchResult.groups[4]?.value?.toDouble() ?: return
        val z = matchResult.groups[5]?.value?.toDouble() ?: return
        sixtySecondBeacon(getColorFromRank(rank) + player, x, y, z)
    }


    @SubscribeEvent
    fun w1(event: ClientChatReceivedEvent) {
        if (!config.waypoints ) return
        val message = StringUtils.stripControlCodes(event.message.unformattedText)
        val matchResult = Regex("Party > (\\[.+\\])? (.{0,16}): x: (-?\\d+) y: (-?\\d+) z: (-?\\d+)").find(message)
        val rank = matchResult?.groups?.get(1)?.value ?: return
        val player = matchResult.groups[2]?.value ?: return
        val x = matchResult.groups[3]?.value?.toDouble() ?: return
        val y = matchResult.groups[4]?.value?.toDouble() ?: return
        val z = matchResult.groups[5]?.value?.toDouble() ?: return
        sixtySecondBeacon(getColorFromRank(rank) + player, x, y, z)
    }

    @SubscribeEvent
    fun w2(event: ClientChatReceivedEvent) {
        if (!config.waypoints ) return
        val message = StringUtils.stripControlCodes(event.message.unformattedText)
        val matchResult = Regex("(\\[.+\\])? (.{0,16}): x: (-?\\d+) y: (-?\\d+) z: (-?\\d+)").find(message)
        val rank = matchResult?.groups?.get(1)?.value ?: return
        val player = matchResult.groups[2]?.value ?: return
        val x = matchResult.groups[3]?.value?.toDouble() ?: return
        val y = matchResult.groups[4]?.value?.toDouble() ?: return
        val z = matchResult.groups[5]?.value?.toDouble() ?: return
        sixtySecondBeacon(getColorFromRank(rank) + player, x, y, z)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!config.waypoints) return
        for (waypoint in waypoints) {
            RenderUtils.renderCustomBeacon(
                waypoint.ign,
                waypoint.x,
                waypoint.y,
                waypoint.z,
                waypoint.r,
                waypoint.g,
                waypoint.b,
                event.partialTicks
            )
        }
    }

    private fun getColorFromRank(rank: String): String {
        return when (rank) {
            "[VIP]", "[VIP+]" -> "§a"
            "[MVP]", "[MVP+]" -> "§b"
            "[MVP++]" -> "§6"
            else -> "§7"
        }
    }
}

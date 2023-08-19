package me.odinclient.features.impl.render

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils.unformattedText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Waypoints : Module(
    name = "Waypoints",
    category = Category.RENDER,
    description = "Custom Waypoints! /wp gui"
) {
    private val vanq: Boolean by BooleanSetting("Vanquisher Spawns")
    private val fromParty: Boolean by BooleanSetting("From Party Chat", true)
    private val fromAll: Boolean by BooleanSetting("From All Chat", true)

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        if (!vanq) return
        val message = event.message.unformattedText.noControlCodes
        val matchResult = Regex("Party > (\\[.+])? (.{0,16}): Vanquisher spawned at: x: (-?\\d+) y: (-?\\d+) z: (-?\\d+)").find(message) ?: return
        val (rank, player) = matchResult.destructured
        val (x, y, z) = matchResult.groupValues.drop(2).map { it.toInt() }
        WaypointManager.addTempWaypoint(getColorFromRank(rank) + player,x,y,z)
    }


    @SubscribeEvent
    fun w1(event: ClientChatReceivedEvent) {
        if (!fromParty) return
        val message = event.unformattedText
        val matchResult = Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+) y: (-?\\d+) z: (-?\\d+)").find(message) ?: return
        val (rank, player) = matchResult.destructured
        val (x, y, z) = matchResult.groupValues.drop(2).map { it.toInt() }
        WaypointManager.addTempWaypoint(getColorFromRank(rank) + player,x,y,z)
    }

    @SubscribeEvent
    fun w2(event: ClientChatReceivedEvent) {
        if (!fromAll) return
        val message = event.unformattedText
        val matchResult = Regex("(\\[.+])? (.{0,16}): x: (-?\\d+) y: (-?\\d+) z: (-?\\d+)").find(message) ?: return
        val (rank, player) = matchResult.destructured
        val (x, y, z) = matchResult.groupValues.drop(2).map { it.toInt() }
        WaypointManager.addTempWaypoint(getColorFromRank(rank) + player,x,y,z)
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

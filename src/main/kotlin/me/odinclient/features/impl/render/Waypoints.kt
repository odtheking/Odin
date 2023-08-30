package me.odinclient.features.impl.render

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.skyblock.ChatUtils.modMessage

object Waypoints : Module(
    name = "Waypoints",
    category = Category.RENDER,
    description = "Custom Waypoints! /wp gui"
) {
    private val vanq: Boolean by BooleanSetting("Vanquisher Spawns")
    private val fromParty: Boolean by BooleanSetting("From Party Chat", true)
    private val fromAll: Boolean by BooleanSetting("From All Chat", true)

    init {
        onMessage(Regex("Party > (\\[.+])? (.{0,16}): Vanquisher spawned at: x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)"), { vanq }) {
            val matchResult = Regex("Party > (\\[.+])? (.{0,16}): Vanquisher spawned at: x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)").find(it) ?: return@onMessage
            val (rank, player) = matchResult.destructured
            val (x, y, z) = matchResult.groupValues.drop(3).map { a -> a.toInt() }
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + player,x,y,z)
        }

        onMessage(Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)"), { fromParty }) {
            val matchResult = Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)").find(it) ?: return@onMessage
            val (rank, player) = matchResult.destructured
            val (x, y, z) = matchResult.groupValues.drop(3).map { a -> a.toInt() }
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + player, x, y, z)
        }

        onMessage(Regex("(?:\\[\\d+])? ?(\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)"), { fromAll }) { // greatest regex of all time!
            val matchResult = Regex("(?:\\[\\d+])? ?(\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)").find(it) ?: return@onMessage
            val (rank, player) = matchResult.destructured
            matchResult.destructured.toList().forEach(::modMessage)
            val (x, y, z) = matchResult.groupValues.drop(3).map { a -> a.toInt() }
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + player,x,y,z)
        }

        onMessage(Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)"), { fromParty }) {
            val matchResult = Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)").find(it) ?: return@onMessage
            val (rank, player) = matchResult.destructured
            val (x, y, z) = matchResult.groupValues.drop(3).map { a -> a.toInt() }
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + player,x,y,z)
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

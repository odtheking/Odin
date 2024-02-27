package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting


object Waypoints : Module(
    name = "Waypoints",
    category = Category.RENDER,
    description = "Custom Waypoints! /wp gui."
) {
    private val fromParty: Boolean by BooleanSetting("From Party Chat", false)
    private val fromAll: Boolean by BooleanSetting("From All Chat", false)
    val onlyBox: Boolean by BooleanSetting("Only shows the box", false)
    val onlyDistance: Boolean by BooleanSetting("Only shows the distance as name", false)


    init {
        onMessage(Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+) "), { fromParty }) {
            val matchResult = Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+) ").find(it) ?: return@onMessage
            val (rank, player) = matchResult.destructured
            val (x, y, z) = matchResult.groupValues.drop(3).map { a -> a.toIntOrNull() ?: return@onMessage }
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + player, x, y, z)
        }

        onMessage(Regex("(?:\\[\\d+])? ?(\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)"), { fromAll }) { // greatest regex of all time!
            val matchResult = Regex("(?:\\[\\d+])? ?(\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)").find(it) ?: return@onMessage
            val (rank, player) = matchResult.destructured
            val (x, y, z) = matchResult.groupValues.drop(3).map { a -> a.toIntOrNull() ?: return@onMessage }
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + player, x, y, z)
        }

        onMessage(Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)"), { fromParty }) {
            val matchResult = Regex("Party > (\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)").find(it) ?: return@onMessage
            val (rank, player) = matchResult.destructured
            val (x, y, z) = matchResult.groupValues.drop(3).map { a -> a.toIntOrNull() ?: return@onMessage }
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + player, x, y, z)
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
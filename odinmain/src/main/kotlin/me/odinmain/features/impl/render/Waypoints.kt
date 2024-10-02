package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting

object Waypoints : Module(
    name = "Waypoints",
    category = Category.RENDER,
    description = "Allows to render waypoints based on coordinates in chat."
) {
    private val fromParty: Boolean by BooleanSetting("From Party Chat", false, description = "Adds waypoints from party chat.")
    private val fromAll: Boolean by BooleanSetting("From All Chat", false, description = "Adds waypoints from all chat.")
    val onlyBox: Boolean by BooleanSetting("Only shows the box", false, description = "Only shows the box, not the name.")
    val onlyDistance: Boolean by BooleanSetting("Only shows the distance as name", false, description = "Only shows the distance as name.")

    private val partyChatRegex = Regex("^Party > (?:\\[\\w+] )?(?:\\[.{1,7}]? )?(.{1,16}): x: (-?\\d+), y: (-?\\d+), z: (-?\\d+).*")
    private val allChatRegex = Regex("(?:\\[\\d+])? (\\[(.{1,7})]? )(.{1,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+).*")

    init {
        onMessage(partyChatRegex, { fromParty && enabled }) {
            val matchResult = partyChatRegex.find(it) ?: return@onMessage
            val (name, x, y, z) = matchResult.destructured
            WaypointManager.addTempWaypoint("§6$name", x.toIntOrNull() ?: return@onMessage, y.toIntOrNull() ?: return@onMessage, z.toIntOrNull() ?: return@onMessage)
        }

        onMessage(allChatRegex, { fromAll && enabled }) {
            val matchResult = allChatRegex.find(it) ?: return@onMessage
            val (name, x, y, z) = matchResult.destructured
            WaypointManager.addTempWaypoint("§6$name", x.toIntOrNull() ?: return@onMessage, y.toIntOrNull() ?: return@onMessage, z.toIntOrNull() ?: return@onMessage)
        }
    }
}
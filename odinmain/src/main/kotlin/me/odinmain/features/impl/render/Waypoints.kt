package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.PositionLook
import me.odinmain.utils.addVec
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.sendCommand
import org.lwjgl.input.Keyboard

object Waypoints : Module(
    name = "Waypoints",
    category = Category.RENDER,
    description = "Allows to render waypoints based on coordinates in chat."
) {
    private val fromParty by BooleanSetting("From Party Chat", true, description = "Adds waypoints from party chat.")
    private val fromAll by BooleanSetting("From All Chat", false, description = "Adds waypoints from all chat.")

    private val pingLocationDropDown by DropdownSetting("Ping Location Dropdown", false)
    private val pingLocationToggle by BooleanSetting("Ping Location", false, description = "Adds a waypoint at the location you are looking at.").withDependency { pingLocationDropDown }
    private val pingLocation by KeybindSetting("Ping Location Keybind", Keyboard.KEY_NONE, description = "Sends the location you are looking at as coords in chat for waypoints.").onPress {
        if (!pingLocationToggle) return@onPress
        EtherWarpHelper.getEtherPos(PositionLook(mc.thePlayer.renderVec, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), pingDistance).pos?.let { pos ->
            val position = pos.addVec(0.5, 0.5, 0.5)
            WaypointManager.addTempWaypoint(x = position.x.toInt(), y = position.y.toInt(), z = position.z.toInt(), time = pingWaypointTime)
            if (sendPingedLocation) sendCommand("odinwaypoint share ${position.x} ${position.y} ${position.z}", true)
        }
    }.withDependency { pingLocationToggle && pingLocationDropDown }
    private val sendPingedLocation: Boolean by BooleanSetting("Send Pinged Location", false, description = "Sends the location you are looking at as coords in chat for waypoints.").withDependency { pingLocationToggle && pingLocationDropDown }
    private val pingWaypointTime by NumberSetting("Ping Waypoint Time", 15000L, 0L, 128000L, 1000L, description = "Time to wait before sending the waypoint command.").withDependency { pingLocationToggle && pingLocationDropDown }
    private val pingDistance by NumberSetting("Ping Distance", 64.0, 1, 128, 1, description = "Distance to ping location.").withDependency { pingLocationToggle && pingLocationDropDown }

    // https://regex101.com/r/3IFer3/4
    private val partyChatRegex = Regex("^Party > (?:\\[\\w+] )?(?:\\[.{1,7}]? )?(.{1,16}): x: (-?\\d+), y: (-?\\d+), z: (-?\\d+).*")
    private val allChatRegex = Regex("(?:\\[\\d+])? (\\[(.{1,7})]? )(.{1,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+).*")

    init {
        onMessage(partyChatRegex, { fromParty && enabled }) {
            val (name, x, y, z) = partyChatRegex.find(it)?.destructured ?: return@onMessage
            WaypointManager.addTempWaypoint("§6$name", x.toIntOrNull() ?: return@onMessage, y.toIntOrNull() ?: return@onMessage, z.toIntOrNull() ?: return@onMessage)
        }

        onMessage(allChatRegex, { fromAll && enabled }) {
            val (name, x, y, z) = partyChatRegex.find(it)?.destructured ?: return@onMessage
            WaypointManager.addTempWaypoint("§6$name", x.toIntOrNull() ?: return@onMessage, y.toIntOrNull() ?: return@onMessage, z.toIntOrNull() ?: return@onMessage)
        }
    }
}
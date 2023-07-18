package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.commandList
import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.display
import me.odinclient.commands.Command
import me.odinclient.ui.clickgui.ClickGUI
import me.odinclient.utils.skyblock.ChatUtils.modMessage

object OdinCommand : Command("odinclient", listOf("od", "odinclient")) {
    override fun executeCommand(args: Array<String>) {
        if (args.isEmpty()) {
            config.openGui()
            return
        }
        when (args[0]) {
            "help" -> modMessage("List of commands: ${commandList.joinToString { it.commandName }}")
            "autosell" -> AutoSellCommand.executeCommand(args.copyOfRange(1, args.size))
            "blacklist" -> BlacklistCommand.executeCommand(args.copyOfRange(1, args.size))
            "esp" -> ESPCommand.executeCommand(args.copyOfRange(1, args.size))
            "waypoint" -> WaypointCommand.executeCommand(args.copyOfRange(1, args.size))
            "clickgui" -> display = ClickGUI
        }
    }
}
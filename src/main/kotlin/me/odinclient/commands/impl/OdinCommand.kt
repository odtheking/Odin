package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.display
import me.odinclient.commands.Command
import me.odinclient.commands.CommandArguments
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.ui.clickgui.ClickGUI

object OdinCommand : Command("odinclient", listOf("od", "odinclient"), "Main command for Odin.") {
    override fun executeCommand(args: CommandArguments) {
        if (args.isEmpty()) {
            display = ClickGUI
            return
        } else {
            ClickGUIModule.resetPositions()
        }
    }

    override val shortcuts: List<String> = listOf("help", "autosell", "blacklist", "esp", "waypoint")
}
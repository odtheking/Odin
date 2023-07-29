package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.display
import me.odinclient.commands.AbstractCommand
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.ui.clickgui.ClickGUI

object OdinCommand : AbstractCommand(
    name = "odinclient",
    arrayListOf("od", "odinclient"),
    "Main command for Odin."
) {
    init {
        empty {
            display = ClickGUI
        }

        "resetgui" does {
            ClickGUIModule.resetPositions()
        }
    }
}
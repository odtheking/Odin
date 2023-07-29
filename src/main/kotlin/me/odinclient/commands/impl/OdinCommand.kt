package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.display
import me.odinclient.commands.AbstractCommand
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.ui.clickgui.ClickGUI
import me.odinclient.utils.skyblock.ChatUtils

object OdinCommand : AbstractCommand("odinclient", "od", "odinclient", description = "Main command for Odin.") {
    init {
        empty {
            display = ClickGUI
        }

        "resetgui" does {
            ClickGUIModule.resetPositions()
            ChatUtils.modMessage("Reset click gui positions.")
        }
    }
}
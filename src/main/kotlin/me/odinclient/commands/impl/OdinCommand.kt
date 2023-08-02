package me.odinclient.commands.impl

import me.odinclient.OdinClient.Companion.display
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.commands.AbstractCommand
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.ui.clickgui.ClickGUI
import me.odinclient.utils.skyblock.ChatUtils.modMessage

object OdinCommand : AbstractCommand("odinclient", "od", "odinclient", description = "Main command for Odin.") {
    init {
        empty {
            display = ClickGUI
        }

        "resetgui" does {
            ClickGUIModule.resetPositions()
            modMessage("Reset click gui positions.")
        }

        // TODO: make a command that sets both and make it not go above vanilla
        "setyaw" does {
            if (it.isEmpty()) return@does modMessage("§cMissing yaw!")
            val yaw = it.first().toFloatOrNull() ?: return@does modMessage("§cInvalid yaw!")
            mc.thePlayer.rotationYaw = yaw
            modMessage("Set yaw to $yaw.")
        }

        "setpitch" does {
            if (it.isEmpty()) return@does modMessage("§cMissing pitch!")
            val pitch = it.first().toFloatOrNull() ?: return@does modMessage("§cInvalid pitch!")
            mc.thePlayer.rotationPitch = pitch
            modMessage("Set pitch to $pitch.")
        }
    }
}
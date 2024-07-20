package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.utils.skyblock.sendCommand
import org.lwjgl.input.Keyboard

object CommandKeybinds : Module(
    name = "Command Keybinds",
    description = "Various keybinds for common skyblock commands.",
    key = null
) {
    private val pets by KeybindSetting("Pets", Keyboard.KEY_NONE, "").onPress {
        sendCommand("pets")
    }
    private val storage by KeybindSetting("Storage", Keyboard.KEY_NONE, "").onPress {
        sendCommand("storage")
    }
    private val dhub by KeybindSetting("Dungeon Hub", Keyboard.KEY_NONE, "").onPress {
        sendCommand("warp dungeon_hub")
    }
}
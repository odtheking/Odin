package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.utils.skyblock.LocationUtils.isInSkyblock
import me.odinmain.utils.skyblock.sendCommand
import org.lwjgl.input.Keyboard

object CommandKeybinds : Module(
    name = "Command Keybinds",
    desc = "Various keybinds for common skyblock commands.",
    key = null
) {
    private val pets by KeybindSetting("Pets", Keyboard.KEY_NONE, desc = "Opens the pets menu.").onPress {
        if (!enabled || !isInSkyblock) return@onPress
        sendCommand("pets")
    }
    private val storage by KeybindSetting("Storage", Keyboard.KEY_NONE, desc = "Opens the storage menu.").onPress {
        if (!enabled || !isInSkyblock) return@onPress
        sendCommand("storage")
    }
    private val wardrobe by KeybindSetting("Wardrobe", Keyboard.KEY_NONE, desc = "Opens the wardrobe menu.").onPress {
        if (!enabled || !isInSkyblock) return@onPress
        sendCommand("wardrobe")
    }
    private val equipment by KeybindSetting("Equipment", Keyboard.KEY_NONE, desc = "Opens the equipment menu.").onPress {
        if (!enabled || !isInSkyblock) return@onPress
        sendCommand("equipment")
    }
    private val dhub by KeybindSetting("Dungeon Hub", Keyboard.KEY_NONE, desc = "Warps to the dungeon hub.").onPress {
        if (!enabled || !isInSkyblock) return@onPress
        sendCommand("warp dungeon_hub")
    }
}

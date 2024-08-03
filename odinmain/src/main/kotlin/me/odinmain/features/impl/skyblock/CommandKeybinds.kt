package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.utils.skyblock.sendCommand
import org.lwjgl.input.Keyboard

object CommandKeybinds : Module(
    name = "Command Keybinds",
    description = "Various keybinds for common skyblock commands.",
    category = Category.SKYBLOCK,
    key = null
) {
    private val pets: Keybinding by KeybindSetting("Pets", Keyboard.KEY_NONE, description = "Opens the pets menu.").onPress {
        sendCommand("pets")
    }
    private val storage: Keybinding by KeybindSetting("Storage", Keyboard.KEY_NONE, description = "Opens the storage menu.").onPress {
        sendCommand("storage")
    }
    private val wardrobe: Keybinding by KeybindSetting("Wardrobe", Keyboard.KEY_NONE, description = "Opens the wardrobe menu.").onPress {
        sendCommand("wardrobe")
    }
    private val equipment: Keybinding by KeybindSetting("Equipment", Keyboard.KEY_NONE, description = "Opens the equipment menu.").onPress {
        sendCommand("equipment")
    }
    private val dhub: Keybinding by KeybindSetting("Dungeon Hub", Keyboard.KEY_NONE, description = "Warps to the dungeon hub.").onPress {
        sendCommand("warp dungeon_hub")
    }
}
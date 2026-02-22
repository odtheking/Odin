package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.LocationUtils
import org.lwjgl.glfw.GLFW

object CommandKeybinds : Module(
    name = "Command Keybinds",
    description = "Various keybinds for common skyblock commands.",
    key = null
) {
    private val pets by KeybindSetting("Pets", GLFW.GLFW_KEY_UNKNOWN, desc = "Opens the pets menu.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("pets")
    }
    private val storage by KeybindSetting("Storage", GLFW.GLFW_KEY_UNKNOWN, desc = "Opens the storage menu.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("storage")
    }
    private val wardrobe by KeybindSetting("Wardrobe", GLFW.GLFW_KEY_UNKNOWN, desc = "Opens the wardrobe menu.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("wardrobe")
    }
    private val equipment by KeybindSetting("Equipment", GLFW.GLFW_KEY_UNKNOWN, desc = "Opens the equipment menu.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("equipment")
    }
    private val dhub by KeybindSetting("Dungeon Hub", GLFW.GLFW_KEY_UNKNOWN, desc = "Warps to the dungeon hub.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("warp dungeon_hub")
    }
    private val potion by KeybindSetting("Potion Bag", GLFW.GLFW_KEY_UNKNOWN, desc = "Open the potion bag.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("potionbag")
    }
}
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
    private val armorSets by KeybindSetting("Armor Sets", GLFW.GLFW_KEY_UNKNOWN, desc = "Opens the wardrobe menu.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("armor")
    }
    private val equipmentSets by KeybindSetting("Equipment Sets", GLFW.GLFW_KEY_UNKNOWN, desc = "Opens the equipment sets menu.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("equipment")
    }
    private val loadouts by KeybindSetting("Loadouts", GLFW.GLFW_KEY_UNKNOWN, desc = "Opens the loadouts menu.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("loadout")
    }
    private val stats by KeybindSetting("Stats", GLFW.GLFW_KEY_UNKNOWN, desc = "Opens the stats menu.").onPress {
        if (!enabled || !LocationUtils.isInSkyblock) return@onPress
        sendCommand("stats")
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

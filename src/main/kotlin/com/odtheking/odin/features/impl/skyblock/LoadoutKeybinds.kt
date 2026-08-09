package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.modMessage
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.lwjgl.glfw.GLFW
import java.security.Key

object LoadoutKeybinds : Module(
    name = "Loadout Keybinds",
    description = "Allows you to use keybinds to navigate the loadout.",
    key = null
) {
    private val nextPageKeybind by KeybindSetting("Next Page", GLFW.GLFW_KEY_RIGHT, desc = "Keybind to go to the next page in the loadout.")
    private val previousPageKeybind by KeybindSetting("Previous Page", GLFW.GLFW_KEY_LEFT, desc = "Keybind to go to the previous page in the loadout.")

    private val advanced by DropdownSetting("Show Settings")
    private val loadout1 by KeybindSetting("Loadout 1", GLFW.GLFW_KEY_1, desc = "Keybind to equip the first loadout slot.").withDependency { advanced }
    private val loadout2 by KeybindSetting("Loadout 2", GLFW.GLFW_KEY_2, desc = "Keybind to equip the second loadout slot.").withDependency { advanced }
    private val loadout3 by KeybindSetting("Loadout 3", GLFW.GLFW_KEY_3, desc = "Keybind to equip the third loadout slot.").withDependency { advanced }
    private val loadout4 by KeybindSetting("Loadout 4", GLFW.GLFW_KEY_4, desc = "Keybind to equip the fourth loadout slot.").withDependency { advanced }
    private val loadout5 by KeybindSetting("Loadout 5", GLFW.GLFW_KEY_5, desc = "Keybind to equip the fifth loadout slot.").withDependency { advanced }
    private val loadout6 by KeybindSetting("Loadout 6", GLFW.GLFW_KEY_6, desc = "Keybind to equip the sixth loadout slot.").withDependency { advanced }
    private val loadout7 by KeybindSetting("Loadout 7", GLFW.GLFW_KEY_7, desc = "Keybind to equip the seventh loadout slot.").withDependency { advanced }
    private val loadout8 by KeybindSetting("Loadout 8", GLFW.GLFW_KEY_8, desc = "Keybind to equip the eighth loadout slot.").withDependency { advanced }
    private val loadout9 by KeybindSetting("Loadout 9", GLFW.GLFW_KEY_9, desc = "Keybind to equip the ninth loadout slot.").withDependency { advanced }
    private val loadout10 by KeybindSetting("Loadout 10", GLFW.GLFW_KEY_0, desc = "Keybind to equip the tenth loadout slot.").withDependency { advanced }
    private val loadout11 by KeybindSetting("Loadout 11", GLFW.GLFW_KEY_MINUS, desc = "Keybind to equip the eleventh loadout slot.").withDependency { advanced }
    private val loadout12 by KeybindSetting("Loadout 12", GLFW.GLFW_KEY_EQUAL, desc = "Keybind to equip the twelfth loadout slot.").withDependency { advanced }

    private val loadoutRegex = Regex("\\((\\d)/(\\d)\\) Loadout")
    private val loadoutSlots = intArrayOf(
        14, 15, 16,
        23, 24, 25,
        32, 33, 34,
        41, 42, 43
    )

    init {
        on<ScreenEvent.MouseClick> {
            if (screen is AbstractContainerScreen<*> && onClick(screen, click.button())) cancel()
        }

        on<ScreenEvent.KeyPress> {
            if (screen is AbstractContainerScreen<*> && onClick(screen, input.key)) cancel()
        }
    }

    private fun onClick(screen: AbstractContainerScreen<*>, keyCode: Int): Boolean {
        val (current, total) = loadoutRegex.find(screen.title.string)?.destructured?.let {
            it.component1().toIntOrNull() to it.component2().toIntOrNull()
        } ?: return false
        if (current == null || total == null) return false

        val index = when (keyCode) {
            nextPageKeybind.value -> if (current < total) 44 else return false
            previousPageKeybind.value -> if (current > 1) 17 else return false
            else -> {
                val keyIndex = arrayOf(loadout1, loadout2, loadout3, loadout4, loadout5, loadout6, loadout7, loadout8, loadout9, loadout10, loadout11, loadout12)
                    .indexOfFirst { it.value == keyCode }
                if (keyIndex == -1) return false

                loadoutSlots.getOrNull(keyIndex) ?: return false
            }
        }

        mc.player?.clickSlot(screen.menu.containerId, index)
        return true
    }
}
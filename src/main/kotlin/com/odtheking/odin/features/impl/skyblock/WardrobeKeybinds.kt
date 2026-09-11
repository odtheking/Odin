package com.odtheking.odin.features.impl.skyblock

import com.mojang.blaze3d.platform.InputConstants
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

object WardrobeKeybinds : Module(
    name = "Wardrobe Keybinds",
    description = "Allows you to use keybinds to navigate the wardrobe.",
    key = null
) {
    private val nextPageKeybind by KeybindSetting("Next Page", InputConstants.KEY_RIGHT, desc = "Keybind to go to the next page in the wardrobe.")
    private val previousPageKeybind by KeybindSetting("Previous Page", InputConstants.KEY_LEFT, desc = "Keybind to go to the previous page in the wardrobe.")
    private val unequipKeybind by KeybindSetting("Unequip", InputConstants.UNKNOWN, desc = "Keybind to unequip the currently equipped item in the wardrobe.")
    private val disallowUnequippingEquipped by BooleanSetting("Disable Unequip", desc = "Prevents unequipping equipped set.")

    private val advanced by DropdownSetting("Show Settings")
    private val wardrobe1 by KeybindSetting("Wardrobe 1", InputConstants.KEY_1, desc = "Keybind to equip the first wardrobe slot.").withDependency { advanced }
    private val wardrobe2 by KeybindSetting("Wardrobe 2", InputConstants.KEY_2, desc = "Keybind to equip the second wardrobe slot.").withDependency { advanced }
    private val wardrobe3 by KeybindSetting("Wardrobe 3", InputConstants.KEY_3, desc = "Keybind to equip the third wardrobe slot.").withDependency { advanced }
    private val wardrobe4 by KeybindSetting("Wardrobe 4", InputConstants.KEY_4, desc = "Keybind to equip the fourth wardrobe slot.").withDependency { advanced }
    private val wardrobe5 by KeybindSetting("Wardrobe 5", InputConstants.KEY_5, desc = "Keybind to equip the fifth wardrobe slot.").withDependency { advanced }
    private val wardrobe6 by KeybindSetting("Wardrobe 6", InputConstants.KEY_6, desc = "Keybind to equip the sixth wardrobe slot.").withDependency { advanced }
    private val wardrobe7 by KeybindSetting("Wardrobe 7", InputConstants.KEY_7, desc = "Keybind to equip the seventh wardrobe slot.").withDependency { advanced }
    private val wardrobe8 by KeybindSetting("Wardrobe 8", InputConstants.KEY_8, desc = "Keybind to equip the eighth wardrobe slot.").withDependency { advanced }
    private val wardrobe9 by KeybindSetting("Wardrobe 9", InputConstants.KEY_9, desc = "Keybind to equip the ninth wardrobe slot.").withDependency { advanced }

    private val wardrobeRegex = Regex("\\((\\d)/(\\d)\\) (Armor|Equipment) Sets")
    private val equippedRegex = Regex("Slot (\\d): Equipped")

    init {
        on<ScreenEvent.MouseClick> {
            if (screen is AbstractContainerScreen<*> && onClick(screen, click.button())) cancel()
        }

        on<ScreenEvent.KeyPress> {
            if (screen is AbstractContainerScreen<*> && onClick(screen, input.key)) cancel()
        }
    }

    private fun onClick(screen: AbstractContainerScreen<*>, keyCode: Int): Boolean {
        val (current, total) = wardrobeRegex.find(screen.title.string)?.destructured?.let {
            it.component1().toIntOrNull() to it.component2().toIntOrNull()
        } ?: return false
        if (current == null || total == null) return false

        val equippedIndex = screen.menu.slots.find { equippedRegex.matches(it.item.hoverName.string) }?.index

        val index = when (keyCode) {
            nextPageKeybind.value -> if (current < total) 53 else return false
            previousPageKeybind.value -> if (current > 1) 45 else return false
            unequipKeybind.value -> equippedIndex ?: return false
            else -> {
                val keyIndex = arrayOf(wardrobe1, wardrobe2, wardrobe3, wardrobe4, wardrobe5, wardrobe6, wardrobe7, wardrobe8, wardrobe9)
                    .indexOfFirst { it.value == keyCode }.takeIf { it != -1 } ?: return false

                if (equippedIndex == keyIndex + 36 && disallowUnequippingEquipped) return modMessage("§cSet already equipped.").let { false }
                keyIndex + 36
            }
        }
        if (disallowUnequippingEquipped && screen.menu.slots[index].item.isEmpty) return false
        mc.player?.clickSlot(index)
        return true
    }
}

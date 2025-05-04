package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.utils.skyblock.getItemIndexInContainerChest
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object WardrobeKeybinds : Module(
    name = "Wardrobe Keybinds",
    desc = "Keybinds for wardrobe equipping and unequipping."
) {
    private val unequipKeybind by KeybindSetting("Unequip Keybind", Keyboard.KEY_NONE, "Unequips the current armor.")
    private val nextPageKeybind by KeybindSetting("Next Page Keybind", Keyboard.KEY_NONE, "Goes to the next page.")
    private val previousPageKeybind by KeybindSetting("Previous Page Keybind", Keyboard.KEY_NONE, "Goes to the previous page.")
    private val delay by NumberSetting("Delay", 300L, 0L, 10000L, 10L, desc = "The delay between each click.", unit = "ms")
    private val disallowUnequippingEquipped by BooleanSetting("Disable Unequip", false, desc = "Prevents unequipping equipped armor.")

    private val advanced by DropdownSetting("Show Settings", false)
    private val wardrobe1 by KeybindSetting("Wardrobe 1", Keyboard.KEY_1, "Wardrobe 1").withDependency { advanced }
    private val wardrobe2 by KeybindSetting("Wardrobe 2", Keyboard.KEY_2, "Wardrobe 2").withDependency { advanced }
    private val wardrobe3 by KeybindSetting("Wardrobe 3", Keyboard.KEY_3, "Wardrobe 3").withDependency { advanced }
    private val wardrobe4 by KeybindSetting("Wardrobe 4", Keyboard.KEY_4, "Wardrobe 4").withDependency { advanced }
    private val wardrobe5 by KeybindSetting("Wardrobe 5", Keyboard.KEY_5, "Wardrobe 5").withDependency { advanced }
    private val wardrobe6 by KeybindSetting("Wardrobe 6", Keyboard.KEY_6, "Wardrobe 6").withDependency { advanced }
    private val wardrobe7 by KeybindSetting("Wardrobe 7", Keyboard.KEY_7, "Wardrobe 7").withDependency { advanced }
    private val wardrobe8 by KeybindSetting("Wardrobe 8", Keyboard.KEY_8, "Wardrobe 8").withDependency { advanced }
    private val wardrobe9 by KeybindSetting("Wardrobe 9", Keyboard.KEY_9, "Wardrobe 9").withDependency { advanced }

    private val wardrobeRegex = Regex("Wardrobe \\((\\d)/(\\d)\\)")
    private val clickCoolDown = Clock(delay)

    @SubscribeEvent
    fun onGuiMouseClick(event: GuiEvent.MouseClick) {
        if (onClick(event.gui as? GuiChest ?: return, event.button - 100)) event.isCanceled = true
    }

    @SubscribeEvent
    fun onGuiKeyPress(event: GuiEvent.KeyPress) {
        if (onClick(event.gui as? GuiChest ?: return, event.key)) event.isCanceled = true
    }

    private fun onClick(gui: GuiChest, key: Int): Boolean {
        val chest = gui.inventorySlots as? ContainerChest ?: return false
        val (current, total) = wardrobeRegex.find(chest.name)?.destructured ?: return false
        val equippedIndex = getItemIndexInContainerChest(chest, "equipped", 36..44, true)

        val index = when {
            nextPageKeybind.isDown() && nextPageKeybind.key == key -> if (current.toInt() < total.toInt()) 53 else return modMessage("§cYou are already on the last page.").let { false }
            previousPageKeybind.isDown() && previousPageKeybind.key == key -> if (current.toInt() > 1) 45 else return modMessage("§cYou are already on the first page.").let { false }
            unequipKeybind.isDown() && unequipKeybind.key == key -> equippedIndex ?: return modMessage("§cCouldn't find equipped armor.").let { false }
            else -> {
                val keyIndex = arrayOf(wardrobe1, wardrobe2, wardrobe3, wardrobe4, wardrobe5, wardrobe6, wardrobe7, wardrobe8, wardrobe9).indexOfFirst { it.isDown() && it.key == key }.takeIf { it != -1 } ?: return false
                if (equippedIndex == keyIndex + 36 && disallowUnequippingEquipped) return modMessage("§cArmor already equipped.").let { false }
                keyIndex + 36
            }
        }

        if (!clickCoolDown.hasTimePassed(delay)) return false
        windowClick(index, ClickType.Left)
        clickCoolDown.update()
        return true
    }
}
package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.getItemIndexInContainerChest
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object WardrobeKeybinds : Module(
    "Wardrobe Keybinds",
    description = "Keybinds for wardrobe equiping and unequipping.",
    category = Category.SKYBLOCK
) {
    private val unequipKeybind: Keybinding by KeybindSetting("Unequip Keybind", Keyboard.KEY_NONE, "Unequips the current armor.")
    private val nextPageKeybind: Keybinding by KeybindSetting("Next Page Keybind", Keyboard.KEY_NONE, "Goes to the next page.")
    private val previousPageKeybind: Keybinding by KeybindSetting("Previous Page Keybind", Keyboard.KEY_NONE, "Goes to the previous page.")
    private val delay: Long by NumberSetting("Delay", 1500, 0.0, 10000.0, 10.0, description = "The delay between each click .")

    private val advanced: Boolean by DropdownSetting("Show Settings", false)

    private val wardrobe1: Keybinding by KeybindSetting("Wardrobe 1", Keyboard.KEY_1, "Wardrobe 1").withDependency { advanced }
    private val wardrobe2: Keybinding by KeybindSetting("Wardrobe 2", Keyboard.KEY_2, "Wardrobe 2").withDependency { advanced }
    private val wardrobe3: Keybinding by KeybindSetting("Wardrobe 3", Keyboard.KEY_3, "Wardrobe 3").withDependency { advanced }
    private val wardrobe4: Keybinding by KeybindSetting("Wardrobe 4", Keyboard.KEY_4, "Wardrobe 4").withDependency { advanced }
    private val wardrobe5: Keybinding by KeybindSetting("Wardrobe 5", Keyboard.KEY_5, "Wardrobe 5").withDependency { advanced }
    private val wardrobe6: Keybinding by KeybindSetting("Wardrobe 6", Keyboard.KEY_6, "Wardrobe 6").withDependency { advanced }
    private val wardrobe7: Keybinding by KeybindSetting("Wardrobe 7", Keyboard.KEY_7, "Wardrobe 7").withDependency { advanced }
    private val wardrobe8: Keybinding by KeybindSetting("Wardrobe 8", Keyboard.KEY_8, "Wardrobe 8").withDependency { advanced }
    private val wardrobe9: Keybinding by KeybindSetting("Wardrobe 9", Keyboard.KEY_9, "Wardrobe 9").withDependency { advanced }

    private val wardrobes = arrayOf(wardrobe1, wardrobe2, wardrobe3, wardrobe4, wardrobe5, wardrobe6, wardrobe7, wardrobe8, wardrobe9)
    private val clickCoolDown = Clock(delay)
    @SubscribeEvent
    fun checkKeybinds(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest) return

        val matchResult = Regex("Wardrobe \\((\\d)/(\\d)\\)").find(chest.name) ?: return
        val (current, total) = matchResult.destructured

        val index = when {
            nextPageKeybind.isDown() -> if (current.toInt() < total.toInt()) 53 else return modMessage("You are already on the last page.")
            previousPageKeybind.isDown() -> if (current.toInt() > 1) 45 else return modMessage("You are already on the first page.")
            unequipKeybind.isDown() -> getItemIndexInContainerChest(chest, "Equipped", 36..44) ?: return modMessage("Couldn't find equipped armor.")
            else -> (wardrobes.indexOfFirst { it.isDown() }.takeIf { it != -1 } ?: return) + 36
        }
        modMessage("Index: $index")
        if (!clickCoolDown.hasTimePassed()) return
        if (index > chest.lowerChestInventory.sizeInventory - 1 || index < 1) return modMessage("Invalid index. $index, ${chest.name}")
        mc.playerController.windowClick(chest.windowId, index, 0, 0, mc.thePlayer)
        clickCoolDown.update()

        event.isCanceled = true
    }
}
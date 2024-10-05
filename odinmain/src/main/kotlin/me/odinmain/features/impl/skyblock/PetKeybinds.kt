package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.*
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object PetKeybinds : Module(
    name = "Pet Keybinds",
    description = "Keybinds for the pets menu. (/petkeys)",
    category = Category.SKYBLOCK
) {
    private val unequipKeybind by KeybindSetting("Unequip Keybind", Keyboard.KEY_NONE, "Unequips the current Pet.")
    private val nextPageKeybind by KeybindSetting("Next Page Keybind", Keyboard.KEY_NONE, "Goes to the next page.")
    private val previousPageKeybind by KeybindSetting("Previous Page Keybind", Keyboard.KEY_NONE, "Goes to the previous page.")
    private val delay by NumberSetting("Delay", 0L, 0, 10000, 10, description = "The delay between each click.", unit = "ms")
    private val nounequip by BooleanSetting("Disable Unequip", default = false, description = "Prevents using a pets keybind to unequip a pet. Does not prevent unequip keybind or normal clicking.")
    private val advanced by DropdownSetting("Show Settings", false)

    private val pet1 by KeybindSetting("Pet 1", Keyboard.KEY_1, "Pet 1 on the list.").withDependency { advanced }
    private val pet2 by KeybindSetting("Pet 2", Keyboard.KEY_2, "Pet 2 on the list.").withDependency { advanced }
    private val pet3 by KeybindSetting("Pet 3", Keyboard.KEY_3, "Pet 3 on the list.").withDependency { advanced }
    private val pet4 by KeybindSetting("Pet 4", Keyboard.KEY_4, "Pet 4 on the list.").withDependency { advanced }
    private val pet5 by KeybindSetting("Pet 5", Keyboard.KEY_5, "Pet 5 on the list.").withDependency { advanced }
    private val pet6 by KeybindSetting("Pet 6", Keyboard.KEY_6, "Pet 6 on the list.").withDependency { advanced }
    private val pet7 by KeybindSetting("Pet 7", Keyboard.KEY_7, "Pet 7 on the list.").withDependency { advanced }
    private val pet8 by KeybindSetting("Pet 8", Keyboard.KEY_8, "Pet 8 on the list.").withDependency { advanced }
    private val pet9 by KeybindSetting("Pet 9", Keyboard.KEY_9, "Pet 9 on the list.").withDependency { advanced }

    private val pets = arrayOf(pet1, pet2, pet3, pet4, pet5, pet6, pet7, pet8, pet9)
    private val clickCoolDown = Clock(delay)

    val petList: MutableList<String> by ListSetting("List", mutableListOf())

    @SubscribeEvent
    fun checkKeybinds(event: GuiEvent.GuiKeyPressEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest) return

        val matchResult = Regex("Pets(?: \\((\\d)/(\\d)\\))?").find(chest.name) ?: return
        val (current, total) = listOf(matchResult.groups[1]?.value?.toIntOrNull() ?: 1, matchResult.groups[2]?.value?.toIntOrNull() ?: 1)

        if (pets.any { it.isDown() } || arrayOf(nextPageKeybind, previousPageKeybind, unequipKeybind).any { it.isDown() }) event.isCanceled = true

        val index = when {
            nextPageKeybind.isDown() -> if (current < total) 53 else return modMessage("You are already on the last page.")
            previousPageKeybind.isDown() -> if (current > 1) 45 else return modMessage("You are already on the first page.")
            unequipKeybind.isDown() -> getItemIndexInContainerChestByLore(chest, "§7§cClick to despawn!", 10..43) ?: return modMessage("Couldn't find equipped pet")
            else -> {
                val petIndex = pets.indexOfFirst { it.isDown() }
                if (petIndex != -1) petList.getOrNull(petIndex)?.let { getItemIndexInContainerChestByUUID(chest, it, 10..43) ?: return modMessage("Couldn't find matching pet or there is no pet in that position.")}
                else return
            }
        }

        if (nounequip && getItemIndexInContainerChestByLore(chest, "§7§cClick to despawn!", 10..43) == index && !unequipKeybind.isDown()) return modMessage("That pet is already equipped!")
        if (!clickCoolDown.hasTimePassed(delay) || index == null) return
        if (index > chest.lowerChestInventory.sizeInventory - 1 || index < 1) return modMessage("Invalid index. $index, ${chest.name}")
        mc.playerController.windowClick(chest.windowId, index, 0, 0, mc.thePlayer)
        clickCoolDown.update()
    }
}
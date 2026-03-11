package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.ListSetting
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.clickSlot
import com.odtheking.odin.utils.itemUUID
import com.odtheking.odin.utils.loreString
import com.odtheking.odin.utils.modMessage
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.lwjgl.glfw.GLFW

object PetKeybinds : Module(
    name = "Pet Keybinds",
    description = "Keybinds for the pets menu. (/petkeys)"
) {
    private val unequipKeybind by KeybindSetting("Unequip", GLFW.GLFW_KEY_UNKNOWN, "Unequips the current Pet.")
    private val nextPageKeybind by KeybindSetting("Next Page", GLFW.GLFW_KEY_UNKNOWN, "Goes to the next page.")
    private val previousPageKeybind by KeybindSetting("Previous Page", GLFW.GLFW_KEY_UNKNOWN, "Goes to the previous page.")
    private val nounequip by BooleanSetting("Disable Unequip", false, desc = "Prevents using a pets keybind to unequip a pet. Does not prevent unequip keybind or normal clicking.")
    private val closeIfAlreadyEquipped by BooleanSetting("Close If Already Equipped", false, "If the pet is already equipped, closes the Pets menu instead.")
    private val advanced by DropdownSetting("Show Settings", false)

    private val pet1 by KeybindSetting("Pet 1", GLFW.GLFW_KEY_1, "Pet 1 on the list.").withDependency { advanced }
    private val pet2 by KeybindSetting("Pet 2", GLFW.GLFW_KEY_2, "Pet 2 on the list.").withDependency { advanced }
    private val pet3 by KeybindSetting("Pet 3", GLFW.GLFW_KEY_3, "Pet 3 on the list.").withDependency { advanced }
    private val pet4 by KeybindSetting("Pet 4", GLFW.GLFW_KEY_4, "Pet 4 on the list.").withDependency { advanced }
    private val pet5 by KeybindSetting("Pet 5", GLFW.GLFW_KEY_5, "Pet 5 on the list.").withDependency { advanced }
    private val pet6 by KeybindSetting("Pet 6", GLFW.GLFW_KEY_6, "Pet 6 on the list.").withDependency { advanced }
    private val pet7 by KeybindSetting("Pet 7", GLFW.GLFW_KEY_7, "Pet 7 on the list.").withDependency { advanced }
    private val pet8 by KeybindSetting("Pet 8", GLFW.GLFW_KEY_8, "Pet 8 on the list.").withDependency { advanced }
    private val pet9 by KeybindSetting("Pet 9", GLFW.GLFW_KEY_9, "Pet 9 on the list.").withDependency { advanced }

    private val petsRegex = Regex("Pets(?: \\((\\d)/(\\d)\\))?")

    val petList by ListSetting("PetKeys List", mutableListOf<String>())

    init {
        on<ScreenEvent.MouseClick> {
            if (screen is AbstractContainerScreen<*> && onClick(screen, click.button())) cancel()
        }

        on<ScreenEvent.KeyPress> {
            if (screen is AbstractContainerScreen<*> && onClick(screen, input.key)) cancel()
        }
    }

    private fun onClick(screen: AbstractContainerScreen<*>, keyCode: Int): Boolean {
        val (current, total) = petsRegex.find(screen.title?.string ?: "")?.destructured?.let {
            (it.component1().toIntOrNull() ?: 1) to (it.component2().toIntOrNull() ?: 1)
        } ?: return false

        var index = when (keyCode) {
            nextPageKeybind.value -> if (current < total) 53 else return false.also { modMessage("§cYou are already on the last page.") }
            previousPageKeybind.value -> if (current > 1) 45 else return false.also { modMessage("§cYou are already on the first page.") }
            unequipKeybind.value ->
                screen.menu.slots.subList(10, 43)
                    .indexOfFirst { it.item?.loreString?.contains("Click to despawn!") == true }
                    .takeIf { it != -1 }?.plus(10) ?: return false.also { modMessage("§cCouldn't find equipped pet") }

            else -> {
                val petIndex =
                    arrayOf(pet1, pet2, pet3, pet4, pet5, pet6, pet7, pet8, pet9).indexOfFirst { it.value == keyCode }
                        .takeIf { it != -1 } ?: return false
                petList.getOrNull(petIndex)?.let { uuid ->
                    screen.menu.slots.subList(10, 43).indexOfFirst { it?.item?.itemUUID == uuid }
                }?.takeIf { it != -1 }?.plus(10)
                    ?: return false.also { modMessage("§cCouldn't find matching pet or there is no pet in that position.") }
            }
        }

        if (screen.menu.slots[index].item?.loreString?.contains("Click to despawn!") == true && unequipKeybind.value != keyCode) {
            modMessage("§cThat pet is already equipped!")
            if (closeIfAlreadyEquipped) index = 49
            else if (nounequip) return false
        }

        mc.player?.clickSlot(screen.menu.containerId, index)
        return true
    }
}
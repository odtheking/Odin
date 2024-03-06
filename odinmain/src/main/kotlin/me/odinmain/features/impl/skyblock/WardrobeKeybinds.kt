package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.GuiKeyPressEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object WardrobeKeybinds : Module(
    "Wardrobe Keybinds",
    description = "Helpful reminders for dungeons.",
    category = Category.SKYBLOCK
) {
    private val unequipKeybind: Keybinding by KeybindSetting("Unequip Keybind", Keyboard.KEY_NONE, "Unequips the current armor.")
    private val nextPageKeybind: Keybinding by KeybindSetting("Next Page Keybind", Keyboard.KEY_NONE, "Goes to the next page.")
    private val previousPageKeybind: Keybinding by KeybindSetting("Previous Page Keybind", Keyboard.KEY_NONE, "Goes to the previous page.")

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

    private val clickCoolDown = Clock(3_000)

    @SubscribeEvent
    fun keyTyped(event: GuiKeyPressEvent) {
        if (
            event.container !is ContainerChest ||
            !event.container.name.startsWith("Wardrobe") ||
            !event.keyCode.equalsOneOf(
                unequipKeybind.key, nextPageKeybind.key, previousPageKeybind.key,
                wardrobe1.key, wardrobe2.key, wardrobe3.key,
                wardrobe4.key, wardrobe5.key, wardrobe6.key,
                wardrobe7.key, wardrobe8.key, wardrobe9.key
            )
        ) return

        val index = when {
            nextPageKeybind.isDown() -> 53
            previousPageKeybind.isDown() -> 45
            unequipKeybind.isDown() -> {
                event.container.inventorySlots.subList(36, 44)
                    .indexOfFirst { it?.stack?.displayName?.noControlCodes?.contains("Equipped") ?: false }
                    .takeIf { it != -1 } ?: return modMessage("Couldn't find equipped armor.")
            }
            else ->  event.keyCode + 34
        }
        if (clickCoolDown.hasTimePassed())
            mc.playerController.windowClick(event.container.windowId, index, 2, 3, mc.thePlayer)

        event.isCanceled = true
    }
}
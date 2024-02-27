package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.GuiKeyPressEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.name
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object WardrobeKeybinds : Module(
    "Reminders",
    description = "Helpful reminders for dungeons.",
    category = Category.SKYBLOCK
) {
    // TODO: add multiple keybind options to clickgui
    // unequip keybind
    // next page keybind
    // previous page keybind
    // equip keybind
    @SubscribeEvent
    fun keyTyped(event: GuiKeyPressEvent) {
        if (
            event.container !is ContainerChest ||
            event.container.name.startsWith("Wardrobe") ||
            event.keyCode !in listOf(2, 3, 4, 5, 6, 7, 8, 9, 10)) return


        mc.playerController.windowClick(event.container.windowId, 35 + event.keyCode, 2, 3, mc.thePlayer)

        event.isCanceled = true
    }
}
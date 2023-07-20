package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CookieClicker : Module(
    "Cookie Clicker",
    category = Category.QOL
) {
    private val cancelSound: Boolean by BooleanSetting("Cancel Sound")

    init {
       /* executor(150) {
            val currentScreen = mc.currentScreen
            if (currentScreen !is GuiChest) return@executor
            val container = currentScreen.inventorySlots
            if (container !is ContainerChest) return@executor
            val chestName = container.lowerChestInventory.displayName.unformattedText

            if (!chestName.startsWith("Cookie Clicker")) return@executor
            mc.playerController.windowClick(
                mc.thePlayer.openContainer.windowId,
                13,
                2,
                3,
                mc.thePlayer
            )
        }

        */
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (!cancelSound) return

        val currentScreen = mc.currentScreen
        if (currentScreen !is GuiChest) return
        val container = currentScreen.inventorySlots
        if (container !is ContainerChest) return

        val chestName = container.lowerChestInventory.displayName.unformattedText
        if (!chestName.startsWith("Cookie Clicker")) return
        if (event.name == "random.eat" && event.sound.volume.toInt() == 1) {
            event.result = null // This should cancel the sound event
        }
    }
}
package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object CookieClicker {

    private var tickRamp = 0
    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (!config.cookieClicker) return
        tickRamp++
        if (tickRamp % 3 != 0) return
        val currentScreen = mc.currentScreen

        if (currentScreen !is GuiChest) return
        val container = currentScreen.inventorySlots
        if (container !is ContainerChest) return
        val chestName = container.lowerChestInventory.displayName.unformattedText

        if (!chestName.startsWith("Cookie Clicker")) return
        mc.playerController.windowClick(
            mc.thePlayer.openContainer.windowId,
            13,
            2,
            3,
            mc.thePlayer
        )

    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        val currentScreen = Minecraft.getMinecraft().currentScreen
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
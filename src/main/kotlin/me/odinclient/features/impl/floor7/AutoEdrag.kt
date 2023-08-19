package me.odinclient.features.impl.floor7

import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils.sendCommand
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoEdrag: Module(
    "Auto Ender Dragon",
    description = "Automatically clicks the Ender Dragon pet at the start of p5.",
    category = Category.FLOOR7
) {
    private var going = false

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (event.message == "[BOSS] Wither King: You.. again?") {
            sendCommand("pets")
            going = true
        }
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        if (!going) return
        PlayerUtils.clickItemInContainer("Pets", "Ender Dragon", event)
    }
}

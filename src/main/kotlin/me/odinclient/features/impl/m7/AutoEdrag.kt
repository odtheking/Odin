package me.odinclient.features.impl.m7

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils.sendCommand
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoEdrag: Module(
    "Auto Ender Dragon",
    category = Category.M7
) {
    private var going = false

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        if (message == "[BOSS] Wither King: You.. again?") {
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

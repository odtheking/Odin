package me.odinclient.features.m7

import me.odinclient.OdinClient.Companion.config
import me.odinclient.utils.skyblock.ChatUtils.sendCommand
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoEdrag {

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!config.autoEdrag) return
        val message = stripControlCodes(event.message.unformattedText).lowercase()
        if (message == "[boss] wither king: you.. again?") {
            sendCommand("pets")
        }
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        if (!config.autoEdrag) return
        PlayerUtils.clickItemInContainer("Pets", "Ender Dragon", event)
    }
}
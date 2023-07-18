package me.odinclient.features.impl.m7

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils.sendCommand
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object AutoEdrag: Module(
    "Auto Ender Dragon",
    Keyboard.KEY_NONE,
    Category.M7
) {
    private var going = false

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!enabled) return
        val message = event.message.unformattedText.noControlCodes.lowercase()
        if (message == "[boss] wither king: you.. again?") {
            sendCommand("pets")
            going = true
        }
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        if (!enabled || !going) return
        PlayerUtils.clickItemInContainer("Pets", "Ender Dragon", event)
    }
}

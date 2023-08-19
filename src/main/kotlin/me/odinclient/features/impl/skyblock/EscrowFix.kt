package me.odinclient.features.impl.skyblock

import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils.sendCommand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EscrowFix : Module(
    name = "Escrow Fix",
    description = "Automatically reopens the ah/bz when it gets closed by escrow",
    category = Category.SKYBLOCK
) {
    private val messages = hashMapOf(
        "Visit the Bazaar to collect your item!" to "bz",
        "Your auction has been sold!" to "ah",
        "You have won an auction!" to "ah",
        "Visit the Auction House to collect your item!" to "ah"
    )

    @SubscribeEvent
    fun onClientChatReceived(event: ChatPacketEvent) {
        messages[event.message]?.let { sendCommand(it) }
    }
}
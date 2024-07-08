package me.odinclient.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.sendCommand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EscrowFix : Module(
    name = "Escrow Fix",
    description = "Automatically reopens the ah/bz when it gets closed by escrow.",
    category = Category.SKYBLOCK
) {
    private val messages = mapOf(
        "There was an error with the auction house! (AUCTION_EXPIRED_OR_NOT_FOUND)" to "ah",
        "There was an error with the auction house! (INVALID_BID)" to "ah",
        "Claiming BIN auction..." to "ah",
        "Visit the Auction House to collect your item!" to "ah"
    )

    private val regex = Regex("Escrow refunded (\\d+) coins for Bazaar Instant Buy Submit!")

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        val command = messages[event.message] ?: if (event.message.matches(regex)) "bz" else null
        command?.let { sendCommand(it) }
    }
}
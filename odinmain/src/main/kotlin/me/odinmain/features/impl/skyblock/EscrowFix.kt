package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.ChatUtils.sendCommand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EscrowFix : Module(
    name = "Escrow Fix",
    description = "Automatically reopens the ah/bz when it gets closed by escrow",
    category = Category.SKYBLOCK,
    tag = TagType.NEW
) {

    private val messages = hashMapOf(
        "There was an error with the auction house! (AUCTION_EXPIRED_OR_NOT_FOUND)" to "ah",
        "There was an error with the auction house! (INVALID_BID)" to "ah",
        "Claiming BIN auction..." to "ah",
        "Visit the Auction House to collect your item!" to "ah",
    )

    val regex = Regex("""Escrow refunded (\d+) coins for Bazaar Instant Buy Submit!""")

    @SubscribeEvent
    fun onClientChatReceived(event: ChatPacketEvent) {
        messages[event.message]?.let { sendCommand(it) }

        if (event.message.matches(regex))
            sendCommand("bz")
    }
}
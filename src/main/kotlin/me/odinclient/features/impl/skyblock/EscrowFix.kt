package me.odinclient.features.impl.skyblock

import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.ChatUtils.sendCommand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EscrowFix : Module(
    name = "AH Re-open",
    description = "Automatically reopens the ah/bz when it gets closed by escrow",
    category = Category.SKYBLOCK,
    tag = TagType.NEW
) {
    //need to get bazzar actual message still
    private val messages = hashMapOf(
        "Visit the Bazaar to collect your item!" to "bz",
        "(AUCTION_EXPIRED_OR_NOT_FOUND)" to "ah",
    )

    @SubscribeEvent
    fun onClientChatReceived(event: ChatPacketEvent) {
        messages[event.message]?.let { sendCommand(it) }
    }
}
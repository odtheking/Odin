package me.odinclient.features.impl.qol

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object EscrowFix : Module(
    name = "Escrow Fix",
    description = "Automatically reopens the ah/bz when escrow happens",
    category = Category.DUNGEON
) {
    //need to get the messages this is just example idfk
    private val messages = arrayOf(
        "Visit the Bazaar to collect your item!" to "bz",
        "Your auction has been sold!" to "ah",
        "You have won an auction!" to "ah",
        "Visit the Auction House to collect your item!" to "ah"
    )
    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes

        for ((m, t) in messages) {
            if (Pattern.matches(m, message)) {
                ChatUtils.sendCommand(t)
            }
        }
    }
}
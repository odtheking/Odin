package me.odinclient.features.impl.qol

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraAlerts : Module(
    "Kuudra Alerts",
    category = Category.QOL
) {
    private val map = mapOf(
        "WARNING: You do not have a key for this tier in your inventory, you will not be able to claim rewards." to "§l§4NO KUUDRA KEY!",
        "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" to "§l§4BUY UPGRADE ROUTE!",
        "[NPC] Elle: Not again!" to "§lPICKUP SUPPLY!",
        "[NPC] Elle: It's time to build the Ballista again! Cover me!" to "§l§4START BUILDING!",
        "Your Fresh Tools Perk bonus doubles your building speed for the next 5 seconds!" to "§l§4EAT FRESH!"
    )

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes

        map[message]?.let {
            PlayerUtils.alert(it)
            return
        }

        Regex("(.+) destroyed one of Kuudra's pods!").find(message)?.let {
            PlayerUtils.alert("§l§4KUUDRA STUNNED!")
            return
        }

        Regex("(.+) is no longer ready!").find(message)?.let {
            PlayerUtils.alert("§l§4${it.groups[1]?.value} IS NO LONGER READY!")
            return
        }
    }
}
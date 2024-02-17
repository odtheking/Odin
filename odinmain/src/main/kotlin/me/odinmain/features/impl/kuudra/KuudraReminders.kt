package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraReminders : Module(
    name = "Kuudra Reminders",
    description = "Displays kuudra information in Kuudra.",
    category = Category.KUUDRA
) {
    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        when (event.message) {
            "WARNING: You do not have a key for this tier in your inventory, you will not be able to claim rewards." -> PlayerUtils.alert("No key in inventory")
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> PlayerUtils.alert("Buy Upgrades")
            "[NPC] Elle: Not again!" -> PlayerUtils.alert("PickUP supplies")
            "[NPC] Elle: It's time to build the Ballista again! Cover me!" -> PlayerUtils.alert("Build Ballista")
            "Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!" -> PlayerUtils.alert("Fresh Tools")
        }
    }
}
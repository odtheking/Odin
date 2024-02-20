package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraReminders : Module(
    name = "Kuudra Reminders",
    description = "Displays kuudra information in Kuudra.",
    category = Category.KUUDRA
) {
    private val displayText: Boolean by BooleanSetting("Display Text", true, description = "Displays kuudra information in chat")
    private val playSound: Boolean by BooleanSetting("Play Sound", true, description = "Plays a sound when a kuudra event occurs")

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        when (event.message) {
            "WARNING: You do not have a key for this tier in your inventory, you will not be able to claim rewards." -> PlayerUtils.alert("No key in inventory", displayText = displayText, playSound = playSound)
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> PlayerUtils.alert("Buy Upgrades", displayText = displayText, playSound = playSound)
            "[NPC] Elle: Not again!" -> PlayerUtils.alert("PickUP supplies", displayText = displayText, playSound = playSound)
            "[NPC] Elle: It's time to build the Ballista again! Cover me!" -> PlayerUtils.alert("Build Ballista", displayText = displayText, playSound = playSound)
            "Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!" -> PlayerUtils.alert("Fresh Tools", displayText = displayText, playSound = playSound)
        }
    }
}
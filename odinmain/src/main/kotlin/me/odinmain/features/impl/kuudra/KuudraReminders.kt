package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraReminders : Module(
    name = "Kuudra Reminders",
    description = "Displays reminders about Kuudra.",
    category = Category.KUUDRA
) {
    private val displayText: Boolean by BooleanSetting("Display Text", true, description = "Displays kuudra information in chat")
    private val playSound: Boolean by BooleanSetting("Play Sound", true, description = "Plays a sound when a kuudra event occurs")
    private val keyReminder: Boolean by BooleanSetting("Key Reminder", true, description = "Reminds you to bring a key")
    private val buyUpgrades: Boolean by BooleanSetting("Buy Upgrades", true, description = "Reminds you to buy upgrades")
    private val pickUpSupplies: Boolean by BooleanSetting("Pick Up Supplies", true, description = "Reminds you to pick up supplies")
    private val buildBallista: Boolean by BooleanSetting("Build Ballista", true, description = "Reminds you to build the ballista")
    private val freshTools: Boolean by BooleanSetting("Fresh Tools", true, description = "Reminds you to use fresh tools")

    init {
        onMessage("WARNING: You do not have a key for this tier in your inventory, you will not be able to claim rewards.", false) {
            if (keyReminder) PlayerUtils.alert("No key in inventory", displayText = displayText, playSound = playSound)
        }
        onMessage("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!", false) {
            if (buyUpgrades) PlayerUtils.alert("Buy Upgrades", displayText = displayText, playSound = playSound)
        }
        onMessage("[NPC] Elle: Not again!", false) {
            if (pickUpSupplies) PlayerUtils.alert("PickUP supplies", displayText = displayText, playSound = playSound)
        }
        onMessage("[NPC] Elle: It's time to build the Ballista again! Cover me!", false) {
            if (buildBallista) PlayerUtils.alert("Build Ballista", displayText = displayText, playSound = playSound)
        }
        onMessage("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!", false) {
            if (freshTools) PlayerUtils.alert("Fresh Tools", displayText = displayText, playSound = playSound)
        }
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        when (event.message) {
            "WARNING: You do not have a key for this tier in your inventory, you will not be able to claim rewards." -> if (keyReminder)
                PlayerUtils.alert("No key in inventory", displayText = displayText, playSound = playSound)
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> if (buyUpgrades)
                PlayerUtils.alert("Buy Upgrades", displayText = displayText, playSound = playSound)
            "[NPC] Elle: Not again!" -> if (pickUpSupplies)
                PlayerUtils.alert("PickUP supplies", displayText = displayText, playSound = playSound)
            "[NPC] Elle: It's time to build the Ballista again! Cover me!" -> if (buildBallista)
                PlayerUtils.alert("Build Ballista", displayText = displayText, playSound = playSound)
            "Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!" -> if (freshTools)
                PlayerUtils.alert("Fresh Tools", displayText = displayText, playSound = playSound)
        }
    }
}
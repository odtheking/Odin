package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.isOtherPlayer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.partyMessage

object KuudraReminders : Module(
    name = "Kuudra Reminders",
    description = "Displays reminders about Kuudra.",
    category = Category.NETHER
) {
    private val displayText by BooleanSetting("Display Text", true, description = "Displays kuudra information in chat.")
    private val playSound by BooleanSetting("Play Sound", true, description = "Plays a sound when a kuudra event occurs.")
    private val keyReminder by BooleanSetting("Key Reminder", true, description = "Reminds you to bring a key.")
    private val buyUpgrades by BooleanSetting("Buy Upgrades", true, description = "Reminds you to buy upgrades.")
    private val pickUpSupplies by BooleanSetting("Pick Up Supplies", true, description = "Reminds you to pick up supplies.")
    private val buildBallista by BooleanSetting("Build Ballista", true, description = "Reminds you to build the ballista.")
    private val freshTools by BooleanSetting("Fresh Tools", true, description = "Reminds you to use fresh tools.")
    private val manaDrain by BooleanSetting("Mana Drain", true, description = "Notifies your party when you use mana on them.")
    private val onlyKuudra by BooleanSetting("Notify in Kuudra Only", true, description = "Notify of mana drain only when in Kuudra.").withDependency { manaDrain }

    init {
        onMessage(Regex("WARNING: You do not have a key for this tier in your inventory, you will not be able to claim rewards."), { keyReminder && enabled }) {
            PlayerUtils.alert("No key in inventory", displayText = displayText, playSound = playSound)
        }

        onMessage(Regex("\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!"), { buyUpgrades && enabled }) {
            PlayerUtils.alert("Buy Upgrades", displayText = displayText, playSound = playSound)
        }

        onMessage(Regex("\\[NPC] Elle: Not again!"), { pickUpSupplies && enabled }) {
            PlayerUtils.alert("Pick up supplies", displayText = displayText, playSound = playSound)
        }

        onMessage(Regex("\\[NPC] Elle: It's time to build the Ballista again! Cover me!"), { buildBallista && enabled }) {
            PlayerUtils.alert("Build Ballista", displayText = displayText, playSound = playSound)
        }

        onMessage(Regex("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!"), { freshTools && enabled }) {
            PlayerUtils.alert("Fresh Tools", displayText = displayText, playSound = playSound)
        }

        onMessage(Regex("Used Extreme Focus! \\((\\d+) Mana\\)"), { manaDrain && enabled && (!onlyKuudra || (onlyKuudra && KuudraUtils.inKuudra))}) {
            val mana = Regex("Used Extreme Focus! \\((\\d+) Mana\\)").find(it)?.groupValues?.get(1)?.toIntOrNull() ?: return@onMessage
            val players = mc.theWorld?.playerEntities?.filter { entity -> entity.isOtherPlayer() && entity.getDistanceSqToEntity(mc.thePlayer) < 49 }?.takeIf { it.isNotEmpty() } ?: return@onMessage
            partyMessage("Used $mana mana on ${players.joinToString(", ") { it.name }}.")
        }
    }
}
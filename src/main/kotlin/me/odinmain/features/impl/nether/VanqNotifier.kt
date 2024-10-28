package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.*

object VanqNotifier: Module(
    name = "Vanq Notifier",
    description = "Notifies you when a vanquisher is nearby.",
    category = Category.NETHER
) {
    private val playSound by BooleanSetting("Play Sound", true, description = "Plays a sound when a vanquisher spawns.")
    private val showText by BooleanSetting("Show Text", true, description = "Shows a message when a vanquisher spawns.")
    private val ac by BooleanSetting("All Chat", false, description = "Sends the message to all chat.")
    private val pc by BooleanSetting("Party Chat", true, description = "Sends the message to party chat.")

   init {
       onMessage("A Vanquisher is spawning nearby!", false) {
           modMessage("Vanquisher has spawned!")
           PlayerUtils.alert("§5Vanquisher", playSound = playSound, displayText = showText)
           if (ac) allMessage(PlayerUtils.getPositionString())
           if (pc) partyMessage(PlayerUtils.getPositionString())
       }
   }
}
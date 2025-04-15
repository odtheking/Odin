package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.allMessage
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage

object VanqNotifier: Module(
    name = "Vanq Notifier",
    desc = "Notifies you when a vanquisher is nearby."
) {
    private val playSound by BooleanSetting("Play Sound", true, desc = "Plays a sound when a vanquisher spawns.")
    private val showText by BooleanSetting("Show Text", true, desc = "Shows a message when a vanquisher spawns.")
    private val ac by BooleanSetting("All Chat", false, desc = "Sends the message to all chat.")
    private val pc by BooleanSetting("Party Chat", true, desc = "Sends the message to party chat.")

   init {
       onMessage(Regex("A Vanquisher is spawning nearby!")) {
           PlayerUtils.alert("§5Vanquisher", playSound = playSound, displayText = showText)
           if (pc) partyMessage(PlayerUtils.getPositionString())
           if (ac) allMessage(PlayerUtils.getPositionString())
           modMessage("§2Vanquisher has spawned!")
       }
   }
}
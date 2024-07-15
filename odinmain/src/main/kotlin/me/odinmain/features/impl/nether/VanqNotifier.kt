package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.floor
import me.odinmain.utils.skyblock.*

object VanqNotifier: Module(
    name = "Vanq Notifier",
    description = "Notifies you when a vanquisher is nearby.",
    category = Category.NETHER
) {
    private val playSound: Boolean by BooleanSetting("Play Sound", true, description = "Plays a sound when a vanquisher spawns")
    private val showText: Boolean by BooleanSetting("Show Text", true, description = "Shows a message when a vanquisher spawns")
    private val ac: Boolean by BooleanSetting("All Chat", false, description = "Sends the message to all chat")
    private val pc: Boolean by BooleanSetting("Party Chat", true, description = "Sends the message to party chat")

   init {
       onMessage("A Vanquisher is spawning nearby!", false) {
           modMessage("Vanquisher has spawned!")
           PlayerUtils.alert("§5Vanquisher", playSound = playSound, displayText = showText)
           if (ac) allMessage("x: ${PlayerUtils.posX.floor()}, y: ${PlayerUtils.posY.floor()}, z: ${PlayerUtils.posZ.floor()}")
           if (pc) partyMessage("x: ${PlayerUtils.posX.floor()}, y: ${PlayerUtils.posY.floor()}, z: ${PlayerUtils.posZ.floor()}")
       }
   }
}
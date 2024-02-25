package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.floor
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.allMessage
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object VanqNotifier: Module(
    name = "Vanq Notifier",
    description = "Notifies you when a vanquisher is nearby.",
    category = Category.KUUDRA
) {
    private val playSound: Boolean by BooleanSetting("Play Sound", true, description = "Plays a sound when a vanquisher spawns")
    private val showText: Boolean by BooleanSetting("Show Text", true, description = "Shows a message when a vanquisher spawns")
    private val ac: Boolean by BooleanSetting("All Chat", false, description = "Sends the message to all chat")
    private val pc: Boolean by BooleanSetting("Party Chat", true, description = "Sends the message to party chat")
    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (event.message != "A Vanquisher is spawning nearby!") return
        modMessage("Vanquisher has spawned!")
        PlayerUtils.alert("§5Vanquisher", playSound = playSound)

        if (ac) allMessage("x: ${PlayerUtils.posX.floor()}, y: ${PlayerUtils.posY.floor()}, z: ${PlayerUtils.posZ.floor()}")
        if (pc) partyMessage("x: ${PlayerUtils.posX.floor()}, y: ${PlayerUtils.posY.floor()}, z: ${PlayerUtils.posZ.floor()}")
    }
}
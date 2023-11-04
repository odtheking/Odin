package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.floor
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ

object VanqNotifier : Module(
    "Vanq Notifier",
    category = Category.SKYBLOCK,
    description = "Sends a message whenever a vanquisher spawns"
) {
    private val ac: Boolean by BooleanSetting("All Chat")
    private val pc: Boolean by BooleanSetting("Party Chat")

    init {
        onMessage(Regex("A Vanquisher is spawning nearby!")) {
            ChatUtils.modMessage("Vanquisher has spawned!")
            PlayerUtils.alert("§5Vanquisher has spawned!")

            if (ac) ChatUtils.sendChatMessage("Vanquisher spawned at: x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}")
            if (pc) ChatUtils.partyMessage("Vanquisher spawned at: x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}")
        }
    }
}
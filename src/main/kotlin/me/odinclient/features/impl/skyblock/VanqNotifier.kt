package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.Utils.floor
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.PlayerUtils.posX
import me.odinclient.utils.skyblock.PlayerUtils.posY
import me.odinclient.utils.skyblock.PlayerUtils.posZ
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object VanqNotifier : Module(
    "Vanq Notifier",
    category = Category.SKYBLOCK,
    description = "Sends a message whenever a vanquisher spawns"
) {
    private val ac: Boolean by BooleanSetting("All chat")
    private val pc: Boolean by BooleanSetting("Party chat")

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText.noControlCodes
        if (message !== "A Vanquisher is spawning nearby!") return

        ChatUtils.modMessage("Vanquisher has spawned!")
        PlayerUtils.alert("§5Vanquisher has spawned!")

        if (ac) ChatUtils.sendChatMessage("Vanquisher spawned at: x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}")
        if (pc) ChatUtils.partyMessage("Vanquisher spawned at: x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}")
    }
}
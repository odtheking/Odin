package me.odinclient.features.impl.skyblock

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.OdinClient
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.skyblock.ChatUtils.sendCommand
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LimboLeave: Module(
    "Auto Limbo Leave",
    description = "Automatically leaves limbo whenever you get kicked.",
    category = Category.SKYBLOCK,
) {
    private val goIsland: Boolean by BooleanSetting("Go to Island", true)

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        // find other message
        if (event.message != "Oops! You are not on SkyBlock so we couldn't warp you!" || inSkyblock) return
        sendCommand("l")
        OdinClient.scope.launch {
            delay(timeMillis = 3000)
            sendCommand("play skyblock")
            if (goIsland) {
                delay(timeMillis = 3000)
                sendCommand("is")
            }
        }
    }
}

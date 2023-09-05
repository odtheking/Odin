package me.odinclient.features.impl.skyblock

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinclient.features.Module
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.Category
import me.odinclient.utils.skyblock.ChatUtils.sendCommand
import me.odinclient.utils.skyblock.LocationUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LimboLeave: Module(
    "Auto Limbo Leave",
    description = "Automatically leaves limbo whenever you get kicked.",
    category = Category.SKYBLOCK,
) {

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (event.message == "Oops! You are not on SkyBlock so we couldn't warp you!") { // need to find the other kicked one
            if(LocationUtils.inSkyblock) return
            sendCommand("l")
            GlobalScope.launch {
                delay(3000)
                sendCommand("play skyblock")
            }
        }
    }
}
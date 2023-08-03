package me.odinclient.features.impl.m7

import me.odinclient.events.ChatPacketEvent
import me.odinclient.events.ServerTickEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NecronDropTimer : Module(
    name = "Necron Drop Timer",
    description = "Shows a timer for when Necron drops you down.",
    category = Category.M7
) {
    var timer: Byte = 0

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (timer > 0) {
            timer--
        }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        if (event.message == "[BOSS] Necron: I'm afraid, your journey ends now.") timer = 60
    }
}
package me.odinclient.events.senders

import me.odinclient.OdinClient
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.ClientSecondEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object ClientSecondEventSender {
    private var tickRamp = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickRamp++

        if (tickRamp % 20 == 0) {
            if (mc.thePlayer != null) MinecraftForge.EVENT_BUS.post(ClientSecondEvent())
            tickRamp = 0
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        tickRamp = 18
    }
}
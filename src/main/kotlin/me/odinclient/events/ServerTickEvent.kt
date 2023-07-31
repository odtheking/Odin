package me.odinclient.events

import me.odinclient.utils.ServerUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ServerTickEvent : Event()

object ServerTickEventSender {
    private var nextTime = 0L

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (System.currentTimeMillis() < nextTime) return
        nextTime = System.currentTimeMillis() + (1000L / ServerUtils.averageTps).toLong()
        MinecraftForge.EVENT_BUS.post(ServerTickEvent())
    }
}
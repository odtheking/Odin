package me.odinclient.events

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.events.impl.ClientSecondEvent
import me.odinclient.events.impl.ReceivePacketEvent
import me.odinclient.events.impl.ServerTickEvent
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.clock.Clock
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object EventDispatcher {

    private var tickRamp = 0

    /** Used to make code simpler. */
    fun post(event: Event) {
        MinecraftForge.EVENT_BUS.post(event)
    }

    /**
     * Dispatches [ChatPacketEvent].
     */
    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet is S02PacketChat) {
            post(ChatPacketEvent(event.packet.chatComponent.unformattedText.noControlCodes))
        }
    }

    /**
     * Dispatches [ClientSecondEvent]
     */
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickRamp++

        if (tickRamp % 20 == 0) {
            if (mc.thePlayer != null) post(ClientSecondEvent())
            tickRamp = 0
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        tickRamp = 18
    }

    // use this. alot more readable and cleaner code. Should be barely less performant cuz everything is inlined.
    private val nextTime = Clock()

    /**
     * Dispatches [ServerTickEvent]
     */
    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (nextTime.hasTimePassed((1000L / ServerUtils.averageTps).toLong(), setTime = true)) {
            post(ServerTickEvent())
        }
    }
}
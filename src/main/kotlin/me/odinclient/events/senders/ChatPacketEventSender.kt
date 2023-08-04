package me.odinclient.events.senders

import me.odinclient.events.ChatPacketEvent
import me.odinclient.events.ReceivePacketEvent
import me.odinclient.utils.Utils.noControlCodes
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ChatPacketEventSender {
    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet is S02PacketChat) {
            MinecraftForge.EVENT_BUS.post(ChatPacketEvent(event.packet.chatComponent.unformattedText.noControlCodes))
        }
    }
}
package me.odinmain.events.impl

import net.hypixel.modapi.packet.ClientboundHypixelPacket
import net.minecraftforge.fml.common.eventhandler.Event


abstract class HypixelPacketEvent : Event() {

    data class ReceivePacketEvent(val packet: ClientboundHypixelPacket) : HypixelPacketEvent()

}
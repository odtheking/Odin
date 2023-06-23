package me.odinclient.events

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class ReceivePacketEvent(val packet: Packet<*>) : Event()

@Cancelable
class PacketSentEvent(val packet: Packet<*>) : Event()
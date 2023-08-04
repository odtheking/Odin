package me.odinclient.events

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinclient.mixin.MixinNetworkManager.onReceivePacket
 */
@Cancelable
class ReceivePacketEvent(val packet: Packet<*>) : Event()

/**
 * @see me.odinclient.mixin.MixinNetworkManager.onSendPacket
 */
@Cancelable
class PacketSentEvent(val packet: Packet<*>) : Event()
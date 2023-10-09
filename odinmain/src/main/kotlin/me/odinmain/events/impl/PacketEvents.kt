package me.odinmain.events.impl

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odin.mixin.MixinNetworkManager.onReceivePacket
 */
@Cancelable
class ReceivePacketEvent(val packet: Packet<*>) : Event()

/**
 * @see me.odin.mixin.MixinNetworkManager.onSendPacket
 */
@Cancelable
class PacketSentEvent(val packet: Packet<*>) : Event()
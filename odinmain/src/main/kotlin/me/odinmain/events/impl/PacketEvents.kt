package me.odinmain.events.impl

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinmain.mixin.MixinNetworkManager.onReceivePacket
 */
@Cancelable
class ReceivePacketEvent(val packet: Packet<*>) : Event()

/**
 * @see me.odinmain.mixin.MixinNetworkManager.onSendPacket
 */
@Cancelable
class PacketSentEvent(var packet: Packet<*>) : Event()
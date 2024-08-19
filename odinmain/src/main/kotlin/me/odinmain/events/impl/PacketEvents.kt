package me.odinmain.events.impl

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * see MixinNetworkManager.onReceivePacket
 */
@Cancelable
class PacketReceivedEvent(val packet: Packet<*>) : Event()

/**
 * see MixinNetworkManager.onSendPacket
 */
@Cancelable
class PacketSentEvent(var packet: Packet<*>) : Event()
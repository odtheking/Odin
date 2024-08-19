package me.odinmain.events.impl

import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * see MixinS1CPacketEntityMetadata.redirectProcessPacket
 */
class PostEntityMetadata(val packet: S1CPacketEntityMetadata) : Event()
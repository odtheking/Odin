package me.odinmain.events.impl

import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinmain.mixin.MixinS1CPacketEntityMetadata.redirectProcessPacket
 * @see me.odinmain.features.impl.floor7.p3.ArrowAlign
 */
class PostEntityMetadata(val packet: S1CPacketEntityMetadata) : Event()
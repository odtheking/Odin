package me.odinclient.events.impl

import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinclient.mixin.MixinS1CPacketEntityMetadata.redirectProcessPacket
 * @see me.odinclient.features.impl.floor7.p3.ArrowAlign
 */
class PostEntityMetadata(val packet: S1CPacketEntityMetadata) : Event()
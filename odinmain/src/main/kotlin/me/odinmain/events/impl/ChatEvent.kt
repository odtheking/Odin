package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Chat packet without formatting.
 * @see me.odinmain.events.EventDispatcher
 */
@Cancelable
data class ChatPacketEvent(val message: String) : Event()

@Cancelable
data class MessageSentEvent(val message: String) : Event()
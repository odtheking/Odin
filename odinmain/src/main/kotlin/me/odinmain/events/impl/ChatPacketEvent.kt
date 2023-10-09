package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Chat packet without formatting.
 * @see me.odin.events.EventDispatcher
 */
class ChatPacketEvent(val message: String) : Event()
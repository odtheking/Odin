package me.odinclient.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Chat packet without formatting.
 * @see me.odinclient.events.EventDispatcher
 */
class ChatPacketEvent(val message: String) : Event()
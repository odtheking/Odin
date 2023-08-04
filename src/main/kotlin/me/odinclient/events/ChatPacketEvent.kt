package me.odinclient.events

import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinclient.events.senders.ChatPacketEventSender
 */
class ChatPacketEvent(val message: String) : Event()
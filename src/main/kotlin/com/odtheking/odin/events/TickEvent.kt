package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event
import net.minecraft.client.multiplayer.ClientLevel

abstract class TickEvent : Event() {
    class Start(val world: ClientLevel) : TickEvent()
    class End(val world: ClientLevel) : TickEvent()
    object Server : TickEvent()
}
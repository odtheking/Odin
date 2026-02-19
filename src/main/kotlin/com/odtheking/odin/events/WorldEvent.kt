package com.odtheking.odin.events

import com.odtheking.odin.events.core.Event

abstract class WorldEvent : Event() {
    object Load : WorldEvent()
    object Unload : WorldEvent()
}
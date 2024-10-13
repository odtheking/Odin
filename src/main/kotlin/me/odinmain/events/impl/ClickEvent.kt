package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

sealed class ClickEvent : Event() {
    @Cancelable
    class Left : ClickEvent()

    @Cancelable
    class Right : ClickEvent()
}

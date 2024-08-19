package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

open class ClickEvent : Event() {
    @Cancelable
    class LeftClickEvent : ClickEvent()

    @Cancelable
    class RightClickEvent : ClickEvent()
}

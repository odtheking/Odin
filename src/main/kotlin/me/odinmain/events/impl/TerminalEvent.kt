package me.odinmain.events.impl

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import net.minecraftforge.fml.common.eventhandler.Event

sealed class TerminalEvent(val type: TerminalTypes) : Event() {
    class Opened(type: TerminalTypes, val solution: List<Int>) : TerminalEvent(type)
    class Closed(type: TerminalTypes) : TerminalEvent(type)
    class Solved(type: TerminalTypes) : TerminalEvent(type)
}

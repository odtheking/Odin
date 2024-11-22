package me.odinmain.events.impl

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import net.minecraftforge.fml.common.eventhandler.Event

open class TerminalEvent(val type: TerminalTypes) : Event() {
    class Opened(type: TerminalTypes) : TerminalEvent(type)
    class Closed(type: TerminalTypes) : TerminalEvent(type)
    class Solved(type: TerminalTypes, val playerName: String, val completionStatus: Int, val total: Int) : TerminalEvent(type)
}
package me.odinmain.events.impl

import me.odinmain.features.impl.floor7.p3.terminalhandler.TerminalHandler
import net.minecraftforge.fml.common.eventhandler.Event

open class TerminalEvent(val terminal: TerminalHandler) : Event() {
    class Opened(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Updated(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Closed(terminal: TerminalHandler) : TerminalEvent(terminal)
    class Solved(terminal: TerminalHandler) : TerminalEvent(terminal)
}
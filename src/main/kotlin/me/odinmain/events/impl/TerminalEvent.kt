package me.odinmain.events.impl

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import net.minecraftforge.fml.common.eventhandler.Event

open class TerminalEvent(val terminal: TerminalSolver.Terminal) : Event() {
    class Opened(terminal: TerminalSolver.Terminal) : TerminalEvent(terminal)
    class Closed(terminal: TerminalSolver.Terminal) : TerminalEvent(terminal)
    class Solved(terminal: TerminalSolver.Terminal) : TerminalEvent(terminal)
}
package me.odinmain.events.impl

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import net.minecraftforge.fml.common.eventhandler.Event

class TerminalOpenedEvent(val type: TerminalTypes, val solution: List<Int>) : Event()

class TerminalClosedEvent(val type: TerminalTypes) : Event()
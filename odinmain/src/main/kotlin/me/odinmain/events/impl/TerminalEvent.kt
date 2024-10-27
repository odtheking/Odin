package me.odinmain.events.impl

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import net.minecraftforge.fml.common.eventhandler.Event

class TerminalOpenedEvent(val type: TerminalTypes) : Event()

class TerminalClosedEvent(val type: TerminalTypes) : Event()

class TerminalSolvedEvent(val type: TerminalTypes, val playerName: String, val completionStatus: Int, val total: Int) : Event()
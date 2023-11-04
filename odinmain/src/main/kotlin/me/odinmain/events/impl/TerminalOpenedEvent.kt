package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

class TerminalOpenedEvent(val type: Int, val solution: List<Int>) : Event()
package me.odinclient.commands.impl

import me.odinclient.commands.AbstractCommand
import me.odinclient.features.impl.floor7.p3.termsim.StartGui

object TerminalSimCommand : AbstractCommand("terminalsimulator", "terminalsim", "termsim", description = "Command for Blacklist.") {
    init {
        empty {
            StartGui.open()
        }
    }
}
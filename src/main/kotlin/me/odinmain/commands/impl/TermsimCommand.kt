package me.odinmain.commands.impl

import com.github.stivais.commodore.Commodore
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.impl.floor7.p3.termsim.StartGUI

val termSimCommand = Commodore("termsim") {
    runs { ping: Long? ->
        StartGUI.open(ping ?: 0)
    }

    runs { terminal: TerminalTypes, ping: Long? ->
        terminal.getSimulator().open(ping ?: 0L)
    }
}
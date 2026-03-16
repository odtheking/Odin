package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.features.impl.floor7.termsim.StartGUI
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes

val termSimCommand = Commodore("termsim") {
    runs { ping: Long? ->
        StartGUI.open(ping ?: 0)
    }

    runs { terminal: TerminalTypes, ping: Long? ->
        terminal.getSimulator().open(ping ?: 0L)
    }
}
package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.features.impl.boss.termsim.StartGUI
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes

val termSimCommand = Commodore("termsim") {
    runs { ping: Long? ->
        schedule(0) { StartGUI.open(ping ?: 0) }
    }

    runs { terminal: TerminalTypes, ping: Long? ->
        schedule(0) { terminal.getSimulator().open(ping ?: 0L) }
    }
}
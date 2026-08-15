package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.boss.termsim.StartGUI
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes

val termSimCommand = Commodore("termsim") {
    runs { ping: Long? ->
        mc.schedule { StartGUI.open(ping ?: 0) }
    }

    runs { terminal: TerminalTypes, ping: Long? ->
        mc.schedule { terminal.getSimulator().open(ping ?: 0L) }
    }
}
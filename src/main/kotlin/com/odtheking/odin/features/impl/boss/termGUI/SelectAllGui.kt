package com.odtheking.odin.features.impl.boss.termGUI

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.equalsOneOf

object SelectAllGui : TermGui() {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 7, 2)

        for (index in 9..slotCount) {
            if ((index % 9).equalsOneOf(0, 8) || index !in currentSolution) continue
            renderSlot(index, TerminalSolver.selectColor)
        }
    }
}
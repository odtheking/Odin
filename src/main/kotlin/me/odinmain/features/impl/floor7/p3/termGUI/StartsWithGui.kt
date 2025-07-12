package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver

object StartsWithGui : TermGui("What starts with:") {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount)

        currentSolution.forEach { pane ->
            renderSlot(pane, TerminalSolver.startsWithColor, TerminalSolver.startsWithColor)
        }
    }
}
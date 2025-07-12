package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver

object SelectAllGui : TermGui("Select all the \"*\" Items!") {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount)

        currentSolution.forEach { pane ->
            renderSlot(pane, TerminalSolver.selectColor, TerminalSolver.selectColor)
        }
    }
}
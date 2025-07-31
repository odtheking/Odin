package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Colors

object SelectAllGui : TermGui() {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 7)

        for (index in 9..slotCount) {
            if ((index % 9).equalsOneOf(0, 8)) continue
            val inSolution = index in currentSolution
            val startColor = if (inSolution) TerminalSolver.selectColor else Colors.TRANSPARENT
            val endColor = if (inSolution) TerminalSolver.selectColor else TerminalSolver.selectColor
            if (colorAnimations[index] != null || inSolution) renderSlot(index, startColor, endColor)
        }
    }
}
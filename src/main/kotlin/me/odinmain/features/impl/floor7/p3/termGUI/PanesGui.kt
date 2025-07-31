package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Colors

object PanesGui : TermGui() {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 5)

        for (index in 9..<slotCount) {
            if ((index % 9).equalsOneOf(0, 1, 7, 8)) continue
            val inSolution = index in currentSolution

            val startColor = if (inSolution) TerminalSolver.panesColor else Colors.TRANSPARENT
            val endColor = if (inSolution) Colors.gray38 else TerminalSolver.panesColor
            renderSlot(index, startColor, endColor)
        }
    }
}
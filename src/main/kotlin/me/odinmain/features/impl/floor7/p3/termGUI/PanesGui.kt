package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Colors

object PanesGui : TermGui("Correct all the panes!") {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount)

        for (index in 9..<slotCount) {
            if ((index % 9).equalsOneOf(0, 1, 7, 8)) continue
            val inSolution = index in currentSolution

            val startColor = if (inSolution) Colors.MINECRAFT_RED else TerminalSolver.panesColor
            val endColor = if (inSolution) TerminalSolver.panesColor else Colors.TRANSPARENT
            renderSlot(index, startColor, endColor)
        }
    }
}
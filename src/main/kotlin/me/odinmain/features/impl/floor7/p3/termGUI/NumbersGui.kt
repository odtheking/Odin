package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.rendering.NVGRenderer

object NumbersGui : TermGui("Click in order!") {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount)
        TerminalSolver.currentTerm?.solution?.forEach { pane ->
            val amount = TerminalSolver.currentTerm?.items[pane]?.stackSize ?: return@forEach
            val index = currentSolution.indexOf(pane)

            val color = when (index) {
                0 -> TerminalSolver.orderColor
                1 -> TerminalSolver.orderColor2
                2 -> TerminalSolver.orderColor3
                else -> Colors.gray38
            }

            val (slotX, slotY) = renderSlot(pane, color, color).let {
                it.first + (50f * TerminalSolver.customTermSize  - NVGRenderer.textWidth(amount.toString(), 30f * TerminalSolver.customTermSize, NVGRenderer.defaultFont)) / 2f to
                        it.second + (50f * TerminalSolver.customTermSize  / 2f + 30f * TerminalSolver.customTermSize  / 2f) / 2f - 6f * TerminalSolver.customTermSize
            }

            if (TerminalSolver.showNumbers && index != -1)
                NVGRenderer.text(amount.toString(), slotX, slotY, 30f * TerminalSolver.customTermSize, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }
}
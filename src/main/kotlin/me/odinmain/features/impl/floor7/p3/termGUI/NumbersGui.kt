package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.rendering.NVGRenderer

object NumbersGui : TermGui() {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 7)

        for (index in 9..slotCount) {
            if ((index % 9).equalsOneOf(0, 8)) continue

            val amount = TerminalSolver.currentTerm?.items?.get(index)?.stackSize ?: continue
            val solutionIndex = currentSolution.indexOf(index)

            val color = when (solutionIndex) {
                0 -> TerminalSolver.orderColor
                1 -> TerminalSolver.orderColor2
                2 -> TerminalSolver.orderColor3
                else -> Colors.TRANSPARENT
            }

            val (slotX, slotY) = renderSlot(index, color, TerminalSolver.orderColor)
            val slotSize = 55f * TerminalSolver.customTermSize
            val fontSize = 30f * TerminalSolver.customTermSize

            val textX = slotX + (slotSize - NVGRenderer.textWidth(amount.toString(), fontSize, NVGRenderer.defaultFont)) / 2f
            val textY = slotY + (slotSize + fontSize) / 2f - fontSize * 0.9f

            if (TerminalSolver.showNumbers && solutionIndex != -1)
                NVGRenderer.textShadow(amount.toString(), textX, textY, 30f * TerminalSolver.customTermSize, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }
}
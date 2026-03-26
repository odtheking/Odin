package com.odtheking.odin.features.impl.boss.termGUI

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import kotlin.math.abs

object NumbersGui : TermGui() {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 7, 2)

        for (index in 9..slotCount) {
            if ((index % 9).equalsOneOf(0, 8)) continue

            val solutionIndex = currentSolution.indexOf(index)
            val amount = abs((currentSolution.size - 14) - solutionIndex) + 1

            val color = when (solutionIndex) {
                0 -> TerminalSolver.orderColor
                1 -> TerminalSolver.orderColor2
                2 -> TerminalSolver.orderColor3
                else -> Colors.TRANSPARENT
            }

            val (slotX, slotY) = renderSlot(index, color)
            val slotSize = 55f * TerminalSolver.customTermSize
            val fontSize = 30f * TerminalSolver.customTermSize

            val textX = slotX + (slotSize - NVGRenderer.textWidth(amount.toString(), fontSize, NVGRenderer.defaultFont)) / 2f
            val textY = slotY + (slotSize + fontSize) / 2f - fontSize * 0.9f

            if (TerminalSolver.showNumbers && solutionIndex != -1)
                NVGRenderer.textShadow(amount.toString(), textX, textY, 30f * TerminalSolver.customTermSize, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }
}
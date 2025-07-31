package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Colors

object MelodyGui : TermGui() {

    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()

        renderTerminal(TerminalSolver.currentTerm?.type?.windowSize ?: 0)
    }

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 7)

        TerminalSolver.currentTerm?.items?.forEachIndexed { index, item ->
            if ((index % 9).equalsOneOf(0, 6, 8) || ((index / 9).equalsOneOf(0, 6) && index % 9 == 7)) return@forEachIndexed
            if ((index !in 9 until 45) && !currentSolution.contains(index)) return@forEachIndexed

            val color = when {
                currentSolution.contains(index) -> {
                    when {
                        (index % 9) in 1..5 && index / 9 != 0 && index / 9 != 5 -> TerminalSolver.melodyPointerColor
                        index / 9 == 0 || index / 9 == 5 -> TerminalSolver.melodyColumColor

                        else -> TerminalSolver.melodyPointerColor
                    }
                }
                (index % 9) in 1..5 && (index / 9).equalsOneOf(1, 2, 3, 4) && currentSolution.any { it / 9 == index / 9 } ->
                    TerminalSolver.melodyRowColor
                else -> Colors.gray38
            }

            renderSlot(index, color, color)
        }
    }
}
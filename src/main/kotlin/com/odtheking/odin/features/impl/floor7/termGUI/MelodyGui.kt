package com.odtheking.odin.features.impl.floor7.termGUI

import com.odtheking.odin.features.impl.floor7.TerminalSolver
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object MelodyGui : TermGui() {

    override fun buildTerminal(screen: AbstractContainerScreen<*>) {
        buildTerminalGrid(screen, rows = 5, cols = 7, startRow = 0, startCol = 1) { index ->
            val row = index / 9
            val col = index % 9
            if (row == 0 || (col == 7 && row in 1..4) || col in 1..5) {
                SlotVisual(resolve = {
                    val color = when {
                        row == 0 -> if (index in currentSolution) TerminalSolver.melodyColumColor else null
                        col == 7 -> if (index in currentSolution) TerminalSolver.melodyPointerColor else TerminalSolver.melodyBackgroundColor
                        else -> if (index in currentSolution) TerminalSolver.melodyPointerColor else TerminalSolver.melodyBackgroundColor
                    }
                    color?.let { it to null }
                })
            } else null
        }
    }
}
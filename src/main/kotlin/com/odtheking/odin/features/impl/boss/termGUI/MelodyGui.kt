package com.odtheking.odin.features.impl.boss.termGUI

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.equalsOneOf
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object MelodyGui : TermGui() {
    override val guiScale get() = TerminalSolver.melodyTermSize

    override fun buildTerminal(screen: AbstractContainerScreen<*>) {
        buildTerminalGrid(screen, rows = 5, cols = 7, startRow = 0, startCol = 1) { index ->
            val row = index / 9
            val col = index % 9
            SlotVisual(resolve = {
                createSlotVisualFromRendering(index).resolve() ?:
                    if (index in currentSolution) TerminalSolver.melodyPointerColor to null else
                    if (!row.equalsOneOf(0, 4) && col != 6) TerminalSolver.melodyBackgroundColor to null else null
            })
        }
    }
}
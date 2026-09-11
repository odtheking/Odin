package com.odtheking.odin.features.impl.boss.termGUI

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object MelodyGui : TermGui() {
    override val guiScale get() = TerminalSolver.melodyTermSize

    override fun buildTerminal(screen: AbstractContainerScreen<*>) {
        buildTerminalGrid(screen, rows = 5, cols = 7, startRow = 0, startCol = 1) { index ->
            val row = index / 9
            val col = index % 9
            val isBorder = row.equalsOneOf(0, 4) || col == 6

            SlotVisual(resolve = {
                TerminalUtils.currentTerm?.getSlotRendering(index) ?: when {
                    index in currentSolution -> TerminalSolver.melodyPointerColor to null
                    !isBorder -> TerminalSolver.melodyBackgroundColor to null
                    else -> null
                }
            }) { x, y, w, h ->
                TerminalUtils.currentTerm?.getSlotRendering(index)?.second
                    ?.let { renderSlotText(it, x, y, w, h, TerminalSolver.textColor) }
            }
        }
    }
}
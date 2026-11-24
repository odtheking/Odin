package com.odtheking.odin.features.impl.boss.termGUI

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils

object MelodyGui : TermGui(rows = 5, cols = 7, startRow = 0, startCol = 1) {
    override val scale get() = TerminalSolver.melodyTermSize

    override fun slotRendering(index: Int): Pair<Color, String?>? {
        val isBorder = (index / 9).equalsOneOf(0, 4) || index % 9 == 6
        return super.slotRendering(index) ?: when {
            index in TerminalUtils.currentTerm?.solution.orEmpty() -> TerminalSolver.melodyPointerColor to null
            !isBorder -> TerminalSolver.melodyBackgroundColor to null
            else -> null
        }
    }
}
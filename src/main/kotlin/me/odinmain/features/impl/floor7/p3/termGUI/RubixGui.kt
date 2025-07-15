package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.rendering.NVGRenderer

object RubixGui : TermGui("Change all to same color!") {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount)

        currentSolution.distinct().forEach { index ->
            val amount = currentSolution.count { it == index }
            val clicksRequired = if (amount < 3) amount else amount - 5
            if (clicksRequired == 0) return@forEach

            val (slotX, slotY) = renderSlot(index, getColor(clicksRequired), getColor(if (amount < 3) clicksRequired + 1 else clicksRequired - 1)).let {
                it.first + (50f * TerminalSolver.customTermSize - NVGRenderer.textWidth(clicksRequired.toString(), 30f * TerminalSolver.customTermSize, NVGRenderer.defaultFont)) / 2f to
                        it.second + (50f * TerminalSolver.customTermSize / 2f + 30f * TerminalSolver.customTermSize / 2f) / 2f - 6f * TerminalSolver.customTermSize
            }

            NVGRenderer.text("$clicksRequired", slotX, slotY, 30f * TerminalSolver.customTermSize, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }

    private fun getColor(clicksRequired: Int): Color = when (clicksRequired) {
        1 -> TerminalSolver.rubixColor1
        2 -> TerminalSolver.rubixColor2
        -1 -> TerminalSolver.oppositeRubixColor1
        else -> TerminalSolver.oppositeRubixColor2
    }
}
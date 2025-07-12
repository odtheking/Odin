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
                it.first + (50f - (textWidths[amount] ?: 0f)) / 2f to it.second + (50f / 2f + 30f / 2f) / 2f - 6f }

            if (TerminalSolver.showNumbers && index != -1)
                NVGRenderer.text(amount.toString(), slotX, slotY, 30f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }

    private val textWidths by lazy {
        (1..14).associateWith { number ->
            NVGRenderer.textWidth(number.toString(), 30f, NVGRenderer.defaultFont)
        }
    }
}
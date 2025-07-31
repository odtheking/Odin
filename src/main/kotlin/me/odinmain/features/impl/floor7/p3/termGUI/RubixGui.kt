package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.rendering.NVGRenderer

object RubixGui : TermGui() {

    override fun renderTerminal(slotCount: Int) {
        renderBackground(slotCount, 3)

        currentSolution.distinct().forEach { index ->
            val amount = currentSolution.count { it == index }
            val clicksRequired = if (amount < 3) amount else amount - 5
            if (clicksRequired == 0) return@forEach

            val (slotX, slotY) = renderSlot(index, getColor(clicksRequired), getColor(if (amount < 3) clicksRequired + 1 else clicksRequired - 1))
            val slotSize = 55f * TerminalSolver.customTermSize
            val fontSize = 30f * TerminalSolver.customTermSize

            val textX = slotX + (slotSize - NVGRenderer.textWidth(clicksRequired.toString(), fontSize, NVGRenderer.defaultFont)) / 2f
            val textY = slotY + (slotSize + fontSize) / 2f - fontSize * 0.9f

            NVGRenderer.textShadow("$clicksRequired", textX, textY, 30f * TerminalSolver.customTermSize, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }

    private fun getColor(clicksRequired: Int): Color = when (clicksRequired) {
        1 -> TerminalSolver.rubixColor1
        2 -> TerminalSolver.rubixColor2
        -1 -> TerminalSolver.oppositeRubixColor1
        else -> TerminalSolver.oppositeRubixColor2
    }
}
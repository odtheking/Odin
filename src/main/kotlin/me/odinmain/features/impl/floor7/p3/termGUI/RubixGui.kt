package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import me.odinmain.features.impl.floor7.p3.TerminalSolver.hideClicked
import me.odinmain.features.impl.floor7.p3.TerminalSolver.textScale
import me.odinmain.utils.render.*

object RubixGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-300, -175, 600, 300, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Change all to same color!", -295, -163, Color.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-298, -135, getTextWidth("Change all to same color!", 20f), 3, Color.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Change all to same color!", 0, -163, Color.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Change all to same color!", 20f) / 2, -135, getTextWidth("Change all to same color!", 20f), 3, Color.WHITE, radius = 5f)
        }
        currentTerm.solution.toSet().forEach { pane ->
            val slot = mc.thePlayer?.inventoryContainer?.inventorySlots?.get(pane) ?: return@forEach

            val needed = currentTerm.solution.count {it == slot.slotIndex}
            val adjusted = if (slot.slotIndex == currentTerm.clickedSlot?.first && currentTerm.clickedSlot?.second?.let { System.currentTimeMillis() - it < 600 } == true && hideClicked) when (needed) {
                3 -> 4
                4 -> 0
                else -> needed - 1
            } else needed

            val text = if (needed < 3) adjusted else (adjusted - 5)

            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val box = BoxWithClass((-168 + ((gap -20).unaryPlus() * 0.5)) + col * 70, -110 + row * 70, 70 - gap, 70 - gap)

            if (adjusted != 0) {
                val color = when (text) {
                    2 -> TerminalSolver.rubixColor2
                    1 -> TerminalSolver.rubixColor1
                    -2 -> TerminalSolver.oppositeRubixColor2
                    else -> TerminalSolver.oppositeRubixColor1
                }
                roundedRectangle(box, color)
                mcText(text.toString(), -168 + col * 70 + 26f , -110 + row * 70 + (27f - (textScale*3) - (gap * 0.5)), 2 + textScale, TerminalSolver.textColor)
            }
            itemIndexMap[pane] = Box(
                box.x.toFloat() * customScale + mc.displayWidth / 2,
                box.y.toFloat() * customScale + mc.displayHeight / 2,
                box.w.toFloat() * customScale,
                box.h.toFloat() * customScale
            )
        }
    }
}
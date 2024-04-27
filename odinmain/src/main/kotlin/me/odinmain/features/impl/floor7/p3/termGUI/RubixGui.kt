package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import me.odinmain.features.impl.floor7.p3.TerminalSolver.solution
import me.odinmain.features.impl.floor7.p3.TerminalSolver.textScale
import me.odinmain.utils.render.*

object RubixGui : TermGui() {

    override fun render() {
        itemIndexMap.clear()
        roundedRectangle(-300, -175, 600, 300, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Change all to same color!", -295, -163, Color.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-298, -135, getTextWidth("Change all to same color!", 20f), 3, Color.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Change all to same color!", 0, -163, Color.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Change all to same color!", 20f) / 2, -135, getTextWidth("Change all to same color!", 20f), 3, Color.WHITE, radius = 5f)
        }
        solution.forEach { pane ->
            val slot = mc.thePlayer.inventoryContainer.inventorySlots[pane]
            val needed = solution.count() {it == slot.slotIndex}
            val text = if (needed < 3) needed.toString() else (needed - 5).toString()

            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val box = BoxWithClass((-168 + ((gap -20).unaryPlus() * 0.5)) + col * 70, -110 + row * 70, 70 - gap, 70 - gap)
            roundedRectangle(box, if (needed < 3) TerminalSolver.rubixColor else TerminalSolver.oppositeRubixColor)
            mcText(text, -168 + col * 70 + 26f , -110 + row * 70 + (27f - (textScale*3) - (gap * 0.5)), 2 + textScale, TerminalSolver.textColor, shadow = TerminalSolver.textShadow)
            itemIndexMap[pane] = Box(
                box.x.toFloat() * customScale + mc.displayWidth / 2,
                box.y.toFloat() * customScale + mc.displayHeight / 2,
                box.w.toFloat() * customScale,
                box.h.toFloat() * customScale
            )
        }
    }
}
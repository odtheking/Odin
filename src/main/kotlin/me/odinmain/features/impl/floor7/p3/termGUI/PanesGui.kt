package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import me.odinmain.features.impl.floor7.p3.TerminalSolver.hideClicked
import me.odinmain.utils.render.*

object PanesGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-300, -150, 600, 300, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Correct All the Panes", -295, -138, Color.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-298, -110, getTextWidth("Correct All the Panes", 20f), 3, Color.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Correct All the Panes", 0, -138, Color.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Correct All the Panes", 20f) / 2, -110, getTextWidth("Correct All the Panes", 20f), 3, Color.WHITE, radius = 5f)
        }
        currentTerm.solution.forEach { pane ->
            if (hideClicked) {
                val slot = mc.thePlayer?.inventoryContainer?.inventorySlots?.get(pane) ?: return@forEach
                if (slot.slotIndex == currentTerm.clickedSlot?.first && currentTerm.clickedSlot?.second?.let { System.currentTimeMillis() - it < 600 } == true) return@forEach
            }
            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val box = BoxWithClass((-168 + ((gap - 20).unaryPlus() * 0.5)) + col * 70, -85 + row * 70, 70 - gap, 70 - gap)
            roundedRectangle(box, TerminalSolver.panesColor)
            itemIndexMap[pane] = Box(
                box.x.toFloat() * customScale + mc.displayWidth / 2,
                box.y.toFloat() * customScale + mc.displayHeight / 2,
                box.w.toFloat() * customScale,
                box.h.toFloat() * customScale
            )
        }
    }
}
package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import me.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor2
import me.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor3
import me.odinmain.features.impl.floor7.p3.TerminalSolver.renderOrderNumbers
import me.odinmain.features.impl.floor7.p3.TerminalSolver.solution
import me.odinmain.features.impl.floor7.p3.TerminalSolver.textScale
import me.odinmain.utils.render.*

object OrderGui : TermGui() {
    override fun render() {
        itemIndexMap.clear()
        roundedRectangle(-300, -125, 600, 225, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Click in order!", -295, -113, Color.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-298, -85, getTextWidth("Click in order!", 20f), 3, Color.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Click in order!", 0, -113, Color.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Click in order!", 20f) / 2, -85, getTextWidth("Click in order!", 20f), 3, Color.WHITE, radius = 5f)
        }
        solution.forEach { pane ->
            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val amount = mc.thePlayer.openContainer.inventorySlots[pane]?.stack?.stackSize ?: 0
            if (amount == 0) return@forEach
            val index = solution.indexOf(pane)
            if (index < 3) {
                val color = when (index) {
                    0    -> orderColor
                    1    -> orderColor2
                    else -> orderColor3
                }
                val box = BoxWithClass((-168 + ((gap-20).unaryPlus() * 0.5)) + col * 290 / 4, -60 + row * 70, 70 - gap, 70 - gap)
                roundedRectangle(box, color)
                itemIndexMap[pane] = Box(
                    box.x.toFloat() * customScale + mc.displayWidth / 2,
                    box.y.toFloat() * customScale + mc.displayHeight / 2,
                    box.w.toFloat() * customScale,
                    box.h.toFloat() * customScale
                )
            }
            if (renderOrderNumbers)
                mcText(amount.toString(), (-167) + col * 290 / 4 + 25f , -57 + row * 70 + 10, 2 + textScale, TerminalSolver.textColor, shadow = TerminalSolver.textShadow)
        }
    }
}
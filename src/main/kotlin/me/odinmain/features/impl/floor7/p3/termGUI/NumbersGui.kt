package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import me.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor2
import me.odinmain.features.impl.floor7.p3.TerminalSolver.orderColor3
import me.odinmain.features.impl.floor7.p3.TerminalSolver.textScale
import me.odinmain.utils.render.*
import me.odinmain.utils.ui.Colors

object NumbersGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-300, -125, 600, 225, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Click in order!", -295, -113, Colors.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-298, -85, getTextWidth("Click in order!", 20f), 3, Colors.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Click in order!", 0, -113, Colors.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Click in order!", 20f) / 2, -85, getTextWidth("Click in order!", 20f), 3, Colors.WHITE, radius = 5f)
        }
        with (TerminalSolver.currentTerm ?: return) {
            solution.forEach { pane ->
                val row = pane / 9 - 1
                val col = pane % 9 - 2
                val amount = items[pane]?.stackSize ?: return@forEach
                val index = solution.indexOf(pane)
                val box = BoxWithClass((-163 + ((gap-20).unaryPlus() * 0.5)) + col * 70, -60 + row * 70, 70 - gap, 70 - gap)
                if (index in 0 until 3) {
                    val color = when (index) {
                        0    -> orderColor
                        1    -> orderColor2
                        else -> orderColor3
                    }
                    roundedRectangle(box, color)
                }
                itemIndexMap[pane] = Box(
                    box.x.toFloat() * customScale + mc.displayWidth / 2,
                    box.y.toFloat() * customScale + mc.displayHeight / 2,
                    box.w.toFloat() * customScale,
                    box.h.toFloat() * customScale
                )

                if (TerminalSolver.showNumbers && index != -1)
                    RenderUtils.drawText(amount.toString(), -163 + col * 70 + 26f , -60f + row * 70f + (27f - (textScale * 3f) - (gap * 0.5f)), 2f + textScale, Colors.WHITE, center = true)
            }
        }

    }
}
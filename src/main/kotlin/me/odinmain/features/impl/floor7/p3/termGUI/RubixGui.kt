package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import me.odinmain.features.impl.floor7.p3.TerminalSolver.textScale
import me.odinmain.utils.render.*
import me.odinmain.utils.ui.Colors

object RubixGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-300, -175, 600, 300, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Change all to same color!", -295, -163, Colors.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-298, -135, getTextWidth("Change all to same color!", 20f), 3, Colors.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Change all to same color!", 0, -163, Colors.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Change all to same color!", 20f) / 2, -135, getTextWidth("Change all to same color!", 20f), 3, Colors.WHITE, radius = 5f)
        }
        currentTerm?.solution?.distinct()?.forEach { pane ->
            val needed = currentTerm?.solution?.count { it == pane } ?: return@forEach
            val text = if (needed < 3) needed else (needed - 5)

            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val box = BoxWithClass((-168 + ((gap -20).unaryPlus() * 0.5)) + col * 70, -110 + row * 70, 70 - gap, 70 - gap)

            if (text != 0) {
                val color = when (text) {
                    2 -> TerminalSolver.rubixColor2
                    1 -> TerminalSolver.rubixColor1
                    -2 -> TerminalSolver.oppositeRubixColor2
                    else -> TerminalSolver.oppositeRubixColor1
                }
                roundedRectangle(box, color)
                RenderUtils.drawText(text.toString(), -168 + col * 70 + 26f , -110f + row * 70f + (27f - (textScale * 3f) - (gap * 0.5f)), 2f + textScale, Colors.WHITE, center = true)
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
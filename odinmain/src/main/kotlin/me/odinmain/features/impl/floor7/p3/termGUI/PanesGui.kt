package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.solution
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.*

object PanesGui : TermGui() {
    override fun render() {
        itemIndexMap.clear()
        roundedRectangle(-300, -150, 600, 300, ColorUtil.moduleButtonColor.withAlpha(.8f), 10f, 1f)
        text("Select All the Panes", -295, -138, Color.WHITE, 20, verticalAlign = TextPos.Top)
        roundedRectangle(-298, -110, getTextWidth("Select All the Panes", 20f), 3, Color.WHITE, radius = 5f)
        solution.forEach { pane ->
            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val box = BoxWithClass(-170 + col * 290 / 4, -85 + row * 70, 50, 50)
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
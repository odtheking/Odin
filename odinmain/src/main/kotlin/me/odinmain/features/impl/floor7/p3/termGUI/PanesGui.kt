package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.solution
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.Gui

object PanesGui : TermGui  {
    override fun render() {
        roundedRectangle(-300, -150, 600, 300, ColorUtil.moduleButtonColor.withAlpha(.8f), 10f, 1f)
        text("Select All the Panes", -295, -138, Color.WHITE, 20, verticalAlign = TextPos.Top)
        roundedRectangle(-298, -110, getTextWidth("Select All the Panes", 20f), 3, Color.WHITE, radius = 5f)
        solution.forEach { pane ->
            val row = pane / 9 - 1
            val col = pane % 9 - 2
            roundedRectangle(-170 + col * 290/4, -85 + row * 70, 50, 50, TerminalSolver.panesColor)
        }
    }
}
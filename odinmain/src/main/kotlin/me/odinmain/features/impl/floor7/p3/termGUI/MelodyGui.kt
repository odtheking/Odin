package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyColumColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyCorrectRowColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyPressColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyPressColumnColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyRowColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.solution
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Box
import me.odinmain.utils.render.BoxWithClass
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.TextAlign
import me.odinmain.utils.render.TextPos
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.roundedRectangle
import me.odinmain.utils.render.text
import me.odinmain.utils.skyblock.modMessage
import kotlin.math.ceil

object MelodyGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-325, -250, 650, 500, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Click the button on time!", -320, -238, Color.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-248, -210, getTextWidth("Click the button on time!", 20f), 3, Color.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Click the button on time!", 0, -238, Color.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Click the button on time!", 20f) / 2, -210, getTextWidth("Click the button on time!", 20f), 3, Color.WHITE, radius = 5f)
        }
        //modMessage("${-110 + 6 * 70}")
        roundedRectangle((-163 + ((gap-20).unaryPlus() * 0.5)) + 5*70, -115,70 - gap, 280 - gap, melodyPressColumnColor)
        solution.forEach { pane ->
            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val colorMelody = when {
                pane / 9 == 0 || pane / 9 == 5 -> melodyColumColor
                (pane % 9).equalsOneOf(1, 2, 3, 4, 5)  -> melodyRowColor
                else -> melodyPressColor
            }
            if ((pane % 9).equalsOneOf(1, 2, 3, 4, 5) && pane / 9 != 0 && pane / 9 != 5) {
                roundedRectangle((-163 + ((gap-20).unaryPlus() * 0.5)) -1*70, -115 + row * 70,350 - gap, 70 - gap, melodyCorrectRowColor)
            }
            val box = BoxWithClass(ceil(-163 + ((gap-20).unaryPlus() * 0.5)) + col * 70, -115 + row * 70, 70 - gap, 70 - gap)
            roundedRectangle(box, colorMelody)
            itemIndexMap[pane] = Box(
                box.x.toFloat() * customScale + mc.displayWidth / 2,
                box.y.toFloat() * customScale + mc.displayHeight / 2,
                box.w.toFloat() * customScale,
                box.h.toFloat() * customScale
            )
        }
    }
}
package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.gap
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyColumColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyCorrectRowColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyPressColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyPressColumColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.melodyRowColor
import me.odinmain.utils.render.*
import me.odinmain.utils.ui.Colors
import net.minecraft.item.Item
import kotlin.math.ceil

object MelodyGui : TermGui() {
    override fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()
        roundedRectangle(-325, -250, 650, 500, TerminalSolver.customGuiColor, 10f, 1f)
        if (TerminalSolver.customGuiText == 0) {
            text("Click the button on time!", -320, -238, Colors.WHITE, 20, verticalAlign = TextPos.Top)
            roundedRectangle(-248, -210, getTextWidth("Click the button on time!", 20f), 3, Colors.WHITE, radius = 5f)
        } else if (TerminalSolver.customGuiText == 1) {
            text("Click the button on time!", 0, -238, Colors.WHITE, 20, align = TextAlign.Middle, verticalAlign = TextPos.Top)
            roundedRectangle(-getTextWidth("Click the button on time!", 20f) / 2, -210, getTextWidth("Click the button on time!", 20f), 3, Colors.WHITE, radius = 5f)
        }

        TerminalSolver.currentTerm?.solution?.forEach { pane ->
            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val colorMelody = when {
                pane / 9 == 0 || pane / 9 == 5 -> melodyColumColor
                (pane % 9) in 1..5  -> melodyRowColor
                else -> melodyPressColor
            }
            if ((pane % 9) in 1..5 && pane / 9 != 0 && pane / 9 != 5) {
                roundedRectangle((-163 + ((gap-20).unaryPlus() * 0.5)) -1*70, -115 + row * 70,350 - gap, 70 - gap, melodyCorrectRowColor)
            }
            val box = BoxWithClass(ceil(-163 + ((gap-20).unaryPlus() * 0.5)) + col * 70, -115 + row * 70, 70 - gap, 70 - gap)
            roundedRectangle(box, colorMelody)
        }

        TerminalSolver.currentTerm?.let {
            it.items.forEachIndexed { index, item ->
                if (Item.getIdFromItem(item?.item) != 159) return@forEachIndexed
                val row = index / 9 - 1
                val col = index % 9 - 2
                val box = BoxWithClass(ceil(-163 + ((gap - 20).unaryPlus() * 0.5)) + col * 70, -115 + row * 70, 70 - gap, 70 - gap)
                if (index !in it.solution) roundedRectangle(box, melodyPressColumColor)
                itemIndexMap[index] = Box(
                    box.x.toFloat() * customScale + mc.displayWidth / 2,
                    box.y.toFloat() * customScale + mc.displayHeight / 2,
                    box.w.toFloat() * customScale,
                    box.h.toFloat() * customScale
                )
            }
        }
    }
}
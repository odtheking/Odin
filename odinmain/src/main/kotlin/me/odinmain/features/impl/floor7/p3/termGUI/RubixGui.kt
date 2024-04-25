package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customScale
import me.odinmain.features.impl.floor7.p3.TerminalSolver.solution
import me.odinmain.utils.render.*

object RubixGui : TermGui() {

    override fun render() {
        itemIndexMap.clear()
        roundedRectangle(-300, -175, 600, 300, TerminalSolver.customGuiColor, 10f, 1f)
        text("What starts with: \"*\"?", -295, -163, Color.WHITE, 20, verticalAlign = TextPos.Top)
        roundedRectangle(-298, -135, getTextWidth("What starts with: \"*\"?", 20f), 3, Color.WHITE, radius = 5f)
        solution.forEach { pane ->
            val slot = mc.thePlayer.inventoryContainer.inventorySlots[pane]
            val needed = solution.count() {it == slot.slotIndex}
            val text = if (needed < 3) needed.toString() else (needed - 5).toString()

            val row = pane / 9 - 1
            val col = pane % 9 - 2
            val box = BoxWithClass(-170 + col * 290 / 4, -110 + row * 70, 50, 50)
            roundedRectangle(box, if (needed < 3) TerminalSolver.rubixColor else TerminalSolver.oppositeRubixColor)
            mcText(text, -170 + col * 290 / 4 + 25f , -110 + row * 70 + 10, 3, TerminalSolver.textColor, shadow = TerminalSolver.textShadow)
            itemIndexMap[pane] = Box(
                box.x.toFloat() * customScale + mc.displayWidth / 2,
                box.y.toFloat() * customScale + mc.displayHeight / 2,
                box.w.toFloat() * customScale,
                box.h.toFloat() * customScale
            )
        }
    }
}
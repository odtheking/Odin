package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.Box
import me.odinmain.utils.render.isPointWithin
import me.odinmain.utils.render.scale
import me.odinmain.utils.render.translate
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.client.gui.ScaledResolution

object CustomTermGui {
    fun render() {
        val sr = ScaledResolution(mc)
        scale(1f / sr.scaleFactor, 1f / sr.scaleFactor)
        translate(mc.displayWidth / 2, mc.displayHeight / 2)
        scale(TerminalSolver.customScale, TerminalSolver.customScale)
        TerminalSolver.currentTerm?.type?.gui?.render()
        scale(1f / TerminalSolver.customScale, 1f / TerminalSolver.customScale)
        translate(-mc.displayWidth / 2, -mc.displayHeight / 2)
        scale(sr.scaleFactor, sr.scaleFactor)
    }

    fun mouseClicked(x: Int, y: Int, button: Int) = TerminalSolver.currentTerm?.type?.gui?.mouseClicked(x, y, button)
}

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()

    fun mouseClicked(x: Int, y: Int, button: Int) {
        itemIndexMap.entries.find { it.value.isPointWithin(x, y) }?.let { (slot, _) ->
            TerminalSolver.currentTerm?.let {
                if (System.currentTimeMillis() - it.timeOpened >= 300 && !GuiEvent.CustomTermGuiClick(slot, button).postAndCatch() && it.canClick(slot, button))
                    it.click(slot, if (button == 0) ClickType.Middle else ClickType.Right, !it.isClicked)
            }
        }
    }

    companion object {
        private var currentGui: TermGui? = null

        fun setCurrentGui(gui: TermGui) {
            currentGui = gui
        }

        fun getHoveredItem(x: Int, y: Int): Int? {
            return currentGui?.itemIndexMap?.entries?.find {
                it.value.isPointWithin(x, y)
            }?.key
        }
    }

    open fun render() {}
}


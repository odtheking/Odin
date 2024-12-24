package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalTypes
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
        when (currentTerm.type) {
            TerminalTypes.PANES -> PanesGui.render()
            TerminalTypes.RUBIX -> RubixGui.render()
            TerminalTypes.ORDER -> OrderGui.render()
            TerminalTypes.STARTS_WITH -> StartsWithGui.render()
            TerminalTypes.SELECT -> SelectAllGui.render()
            TerminalTypes.MELODY -> MelodyGui.render()
            TerminalTypes.NONE -> {}
        }
        scale(1f / TerminalSolver.customScale, 1f / TerminalSolver.customScale)
        translate(-mc.displayWidth / 2, -mc.displayHeight / 2)
        scale(sr.scaleFactor, sr.scaleFactor)
    }

    fun mouseClicked(x: Int, y: Int, button: Int) {
        when (currentTerm.type) {
            TerminalTypes.PANES -> PanesGui.mouseClicked(x, y, button)
            TerminalTypes.RUBIX -> RubixGui.mouseClicked(x, y, button)
            TerminalTypes.ORDER -> OrderGui.mouseClicked(x, y, button)
            TerminalTypes.STARTS_WITH -> StartsWithGui.mouseClicked(x, y, button)
            TerminalTypes.SELECT -> SelectAllGui.mouseClicked(x, y, button)
            TerminalTypes.MELODY -> MelodyGui.mouseClicked(x, y, button)
            TerminalTypes.NONE -> return
        }
    }
}

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()

    fun mouseClicked(x: Int, y: Int, button: Int) {
        itemIndexMap.entries.find { it.value.isPointWithin(x, y) }?.let {
            if (System.currentTimeMillis() - currentTerm.timeOpened < 300) return
            val needed = currentTerm.solution.count { slot -> slot == it.key }
            if (currentTerm.type == TerminalTypes.RUBIX && ((needed < 3 && button != 0) || (needed >= 3 && button != 1))) return
            if (GuiEvent.CustomTermGuiClick(it.key, if (button == 0) 3 else 0, button).postAndCatch()) return
            windowClick(it.key, if (button == 1) ClickType.Right else ClickType.Middle, true)
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


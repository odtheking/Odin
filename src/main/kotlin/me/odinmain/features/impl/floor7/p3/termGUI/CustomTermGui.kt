package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.canClick
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
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
        currentTerm.type.gui?.render()
        scale(1f / TerminalSolver.customScale, 1f / TerminalSolver.customScale)
        translate(-mc.displayWidth / 2, -mc.displayHeight / 2)
        scale(sr.scaleFactor, sr.scaleFactor)
    }

    fun mouseClicked(x: Int, y: Int, button: Int) = currentTerm.type.gui?.mouseClicked(x, y, button)
}

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()

    fun mouseClicked(x: Int, y: Int, button: Int) {
        itemIndexMap.entries.find { it.value.isPointWithin(x, y) }?.let { (slot, _) ->
            if (System.currentTimeMillis() - currentTerm.timeOpened < 300 || GuiEvent.CustomTermGuiClick(slot, button).postAndCatch() || !canClick(slot, button) ) return
            if (currentTerm.clickedSlot?.second?.let { System.currentTimeMillis() - it < 600 } != true) currentTerm.clickedSlot = slot to System.currentTimeMillis()
            windowClick(slot, if (button == 1) ClickType.Right else ClickType.Middle)
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


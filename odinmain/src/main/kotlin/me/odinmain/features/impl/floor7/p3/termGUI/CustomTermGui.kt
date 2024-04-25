package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.render.*
import net.minecraft.client.gui.ScaledResolution

object CustomTermGui {
    fun render() {
        val sr = ScaledResolution(mc)
        scale(1f / sr.scaleFactor, 1f / sr.scaleFactor)
        translate(mc.displayWidth / 2, mc.displayHeight / 2)
        scale(TerminalSolver.customScale, TerminalSolver.customScale)
        when (currentTerm) {
            TerminalTypes.PANES -> PanesGui.render()
            TerminalTypes.COLOR -> RubixGui.render()
            TerminalTypes.ORDER -> OrderGui.render()
            TerminalTypes.STARTS_WITH -> StartsWithGui.render()
            TerminalTypes.SELECT -> SelectAllGui.render()
            TerminalTypes.NONE -> {}
        }
        scale(1f / TerminalSolver.customScale, 1f / TerminalSolver.customScale)
        translate(-mc.displayWidth / 2, -mc.displayHeight / 2)
        scale(sr.scaleFactor, sr.scaleFactor)
    }

    fun mouseClicked(x: Int, y: Int) {
        when (currentTerm) {
            TerminalTypes.PANES -> PanesGui.mouseClicked(x, y)
            TerminalTypes.COLOR -> RubixGui.mouseClicked(x, y)
            TerminalTypes.ORDER -> OrderGui.mouseClicked(x, y)
            TerminalTypes.STARTS_WITH -> StartsWithGui.mouseClicked(x, y)
            TerminalTypes.SELECT -> SelectAllGui.mouseClicked(x, y)
            TerminalTypes.NONE -> return
        }
    }
}

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()

    fun mouseClicked(x: Int, y: Int) {
        itemIndexMap.entries.find {
            it.value.isPointWithin(x, y)
        }?.let {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, it.key, 2, 3, mc.thePlayer)
        }
    }

    open fun render() {}
}
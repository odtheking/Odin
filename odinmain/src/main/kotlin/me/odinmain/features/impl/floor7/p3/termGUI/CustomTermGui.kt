package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.utils.render.*
import net.minecraft.client.gui.ScaledResolution

open class CustomTermGui {
    open fun render() {}

    companion object {
        fun render() {
            val sr = ScaledResolution(mc)
            scale(1f / sr.scaleFactor, 1f / sr.scaleFactor)
            translate(mc.displayWidth / 2, mc.displayHeight / 2)
            scale(TerminalSolver.customScale, TerminalSolver.customScale)
            when (currentTerm) {
                0 -> PanesGui.render()
                1 -> RubixGui.render()
                2 -> OrderGui.render()
                3 -> StartsWithGui.render()
                4 -> SelectAllGui.render()
            }
            scale(1f / TerminalSolver.customScale, 1f / TerminalSolver.customScale)
            translate(-mc.displayWidth / 2, -mc.displayHeight / 2)
            scale(sr.scaleFactor, sr.scaleFactor)
        }

        fun mouseClicked(x: Int, y: Int) {
            when (currentTerm) {
                0 -> PanesGui.mouseClicked(x, y)
                1 -> RubixGui.mouseClicked(x, y)
                2 -> OrderGui.mouseClicked(x, y)
                3 -> StartsWithGui.mouseClicked(x, y)
                4 -> SelectAllGui.mouseClicked(x, y)
            }
        }
    }
}

interface TermGui {
    val itemIndexMap: MutableMap<Int, Box>
    fun render()
    fun mouseClicked(x: Int, y: Int): Boolean
}
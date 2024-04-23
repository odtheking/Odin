package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.utils.render.scale
import me.odinmain.utils.render.translate
import me.odinmain.utils.skyblock.modMessage
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
    }
}

interface TermGui {
    fun render()
}
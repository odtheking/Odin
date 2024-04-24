package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.render.scale
import me.odinmain.utils.render.translate
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
                TerminalTypes.PANES -> PanesGui.render()
                TerminalTypes.COLOR -> RubixGui.render()
                TerminalTypes.ORDER -> OrderGui.render()
                TerminalTypes.STARTS_WITH -> StartsWithGui.render()
                TerminalTypes.SELECT -> SelectAllGui.render()
                TerminalTypes.NONE -> {
                    scale(1f / TerminalSolver.customScale, 1f / TerminalSolver.customScale)
                    translate(-mc.displayWidth / 2, -mc.displayHeight / 2)
                    scale(sr.scaleFactor, sr.scaleFactor)
                    return
                }
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
package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.hideClicked
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.Box
import me.odinmain.utils.render.isPointWithin
import me.odinmain.utils.skyblock.ClickType
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager

object CustomTermGui {
    fun render() {
        val sr = ScaledResolution(mc)
        GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 0f)
        GlStateManager.translate(mc.displayWidth / 2f, mc.displayHeight / 2f, 0f)
        GlStateManager.scale(TerminalSolver.customScale, TerminalSolver.customScale, 0f)
        TerminalSolver.currentTerm?.type?.gui?.render()
        GlStateManager.scale(1f / TerminalSolver.customScale, 1f / TerminalSolver.customScale, 0f)
        GlStateManager.translate(-mc.displayWidth / 2f, -mc.displayHeight / 2f, 0f)
        GlStateManager.scale(sr.scaleFactor.toDouble(), sr.scaleFactor.toDouble(), 0.0)
    }

    fun mouseClicked(x: Int, y: Int, button: Int) = TerminalSolver.currentTerm?.type?.gui?.mouseClicked(x, y, button)
}

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()

    fun mouseClicked(x: Int, y: Int, button: Int) {
        itemIndexMap.entries.find { it.value.isPointWithin(x, y) }?.let { (slot, _) ->
            TerminalSolver.currentTerm?.let {
                if (System.currentTimeMillis() - it.timeOpened >= 300 && !GuiEvent.CustomTermGuiClick(slot, button).postAndCatch() && it.canClick(slot, button))
                    it.click(slot, if (button == 0) ClickType.Middle else ClickType.Right, hideClicked && !it.isClicked)
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


package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalSolver.openedTerminalTime
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils.isPointWithin
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager

object CustomTermGui {
    fun render() {
        val sr = ScaledResolution(mc)
        GlStateManager.scale(1.0 / sr.scaleFactor, 1.0 / sr.scaleFactor, 0.0)
        GlStateManager.translate(mc.displayWidth / 2.0, mc.displayHeight / 2.0, 0.0)
        GlStateManager.scale(TerminalSolver.customScale.toDouble(), TerminalSolver.customScale.toDouble(), 0.0)
        when (currentTerm) {
            TerminalTypes.PANES -> PanesGui.render()
            TerminalTypes.RUBIX -> RubixGui.render()
            TerminalTypes.ORDER -> OrderGui.render()
            TerminalTypes.STARTS_WITH -> StartsWithGui.render()
            TerminalTypes.SELECT -> SelectAllGui.render()
            TerminalTypes.MELODY -> {}
            TerminalTypes.NONE -> {}
        }
        GlStateManager.scale(1.0 / TerminalSolver.customScale, 1.0 / TerminalSolver.customScale, 0.0)
        GlStateManager.translate(-mc.displayWidth / 2.0, -mc.displayHeight / 2.0, 0.0)
        GlStateManager.scale(sr.scaleFactor.toDouble(), sr.scaleFactor.toDouble(), 0.0)
    }

    fun mouseClicked(x: Int, y: Int, button: Int) {
        when (currentTerm) {
            TerminalTypes.PANES -> PanesGui.mouseClicked(x, y, button)
            TerminalTypes.RUBIX -> RubixGui.mouseClicked(x, y, button)
            TerminalTypes.ORDER -> OrderGui.mouseClicked(x, y, button)
            TerminalTypes.STARTS_WITH -> StartsWithGui.mouseClicked(x, y, button)
            TerminalTypes.SELECT -> SelectAllGui.mouseClicked(x, y, button)
            TerminalTypes.MELODY -> return
            TerminalTypes.NONE -> return
        }
    }
}

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, RenderUtils.Box> = mutableMapOf()

    fun mouseClicked(x: Int, y: Int, button: Int) {
        itemIndexMap.entries.find {
            it.value.isPointWithin(x, y)
        }?.let {
            if (System.currentTimeMillis() - openedTerminalTime < 300) return
            if (GuiEvent.CustomTermGuiClick(it.key, if (button == 0) 3 else 0, button).postAndCatch()) return
            windowClick(it.key, if (button == 0) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Right, true)
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


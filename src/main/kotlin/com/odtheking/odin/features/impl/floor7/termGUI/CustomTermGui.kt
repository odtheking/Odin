package com.odtheking.odin.features.impl.floor7.termGUI

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.TerminalSolver.hideClicked
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import com.odtheking.odin.utils.ui.isAreaHovered
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.gui.screens.Screen
import kotlin.math.ceil
import kotlin.math.floor

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()
    inline val currentSolution get() = TerminalUtils.currentTerm?.solution.orEmpty()

    abstract fun renderTerminal(slotCount: Int)

    protected fun renderBackground(slotCount: Int, slotWidth: Int, rowOffset: Int) {
        val slotSize = 55f * TerminalSolver.customTermSize
        val gap = TerminalSolver.gap * TerminalSolver.customTermSize
        val totalSlotSpace = slotSize + gap

        val backgroundStartX = mc.window.screenWidth / 2f + - (slotWidth / 2f) * totalSlotSpace - 7.5f * TerminalSolver.customTermSize
        val backgroundStartY = mc.window.screenHeight / 2f + ((-rowOffset + 0.5f) * totalSlotSpace) - 7.5f * TerminalSolver.customTermSize
        val backgroundWidth = slotWidth * totalSlotSpace + 15f * TerminalSolver.customTermSize
        val backgroundHeight = ((slotCount) / 9) * totalSlotSpace + 15f * TerminalSolver.customTermSize

        NVGRenderer.rect(backgroundStartX, backgroundStartY, backgroundWidth, backgroundHeight, TerminalSolver.backgroundColor.rgba, 12f)
    }

    protected fun renderSlot(index: Int, color: Color): Pair<Float, Float> {
        val slotSize = 55f * TerminalSolver.customTermSize
        val totalSlotSpace = slotSize + TerminalSolver.gap * TerminalSolver.customTermSize

        val x = (index % 9 - 4) * totalSlotSpace + mc.window.screenWidth / 2f - slotSize / 2
        val y = (index / 9 - 2) * totalSlotSpace + mc.window.screenHeight / 2f - slotSize / 2

        itemIndexMap[index] = Box(x, y, slotSize, slotSize)

        NVGRenderer.rect(floor(x), floor(y), ceil(slotSize), ceil(slotSize), color.rgba, TerminalSolver.roundness)
        return x to y
    }

    fun mouseClicked(screen: Screen, button: Int) {
        getHoveredItem()?.let { slot ->
            TerminalUtils.currentTerm?.let {
                if (System.currentTimeMillis() - it.timeOpened >= TerminalSolver.firstClickProt && !GuiEvent.CustomTermGuiClick(screen, slot, button).postAndCatch() && it.canClick(slot, button))
                    it.click(slot, button, hideClicked && !it.isClicked)
            }
        }
    }

    open fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()

        renderTerminal(TerminalUtils.currentTerm?.type?.windowSize?.minus(10) ?: 0)
    }

    companion object {
        private var currentGui: TermGui? = null

        fun setCurrentGui(gui: TermGui) {
            currentGui = gui
        }

        fun getHoveredItem(): Int? =
            currentGui?.itemIndexMap?.entries?.find { isAreaHovered(it.value.x, it.value.y, it.value.w, it.value.h) }?.key
    }

    data class Box(val x: Float, val y: Float, val w: Float, val h: Float)
}
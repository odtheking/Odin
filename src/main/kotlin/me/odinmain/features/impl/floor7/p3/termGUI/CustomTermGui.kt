package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.hideClicked
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.ui.animations.ColorAnimation
import me.odinmain.utils.ui.isAreaHovered
import me.odinmain.utils.ui.rendering.NVGRenderer
import kotlin.math.ceil
import kotlin.math.floor

abstract class TermGui {
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()
    inline val currentSolution get() = TerminalSolver.currentTerm?.solution.orEmpty()
    val colorAnimations = mutableMapOf<Int, ColorAnimation>()

    abstract fun renderTerminal(slotCount: Int)

    protected fun renderBackground(slotCount: Int, slotWidth: Int) {
        val slotSize = 55f * TerminalSolver.customTermSize
        val gap = TerminalSolver.gap * TerminalSolver.customTermSize
        val totalSlotSpace = slotSize + gap

        val backgroundStartX = mc.displayWidth / 2f + -(slotWidth / 2f) * totalSlotSpace - 7.5f * TerminalSolver.customTermSize
        val backgroundStartY = mc.displayHeight / 2f + ((-getRowOffset(slotCount) + 0.5f) * totalSlotSpace) - 7.5f * TerminalSolver.customTermSize
        val backgroundWidth = slotWidth * totalSlotSpace + 15f * TerminalSolver.customTermSize
        val backgroundHeight = ((slotCount) / 9) * totalSlotSpace + 15f * TerminalSolver.customTermSize

        NVGRenderer.rect(backgroundStartX, backgroundStartY, backgroundWidth, backgroundHeight, TerminalSolver.backgroundColor.rgba, 12f)
    }

    protected fun renderSlot(index: Int, startColor: Color, endColor: Color): Pair<Float, Float> {
        val slotSize = 55f * TerminalSolver.customTermSize
        val totalSlotSpace = slotSize + TerminalSolver.gap * TerminalSolver.customTermSize

        val x = (index % 9 - 4) * totalSlotSpace + mc.displayWidth / 2f - slotSize / 2
        val y = (index / 9 - 2) * totalSlotSpace + mc.displayHeight / 2f - slotSize / 2

        itemIndexMap[index] = Box(x, y, slotSize, slotSize)

        val colorAnim = colorAnimations.getOrPut(index) { ColorAnimation(250) }

        NVGRenderer.rect(floor(x), floor(y), ceil(slotSize), ceil(slotSize), colorAnim.get(startColor, endColor, true).rgba, TerminalSolver.roundness)
        return x to y
    }

    fun mouseClicked(button: Int) {
        getHoveredItem()?.let { slot ->
            TerminalSolver.currentTerm?.let {
                if (System.currentTimeMillis() - it.timeOpened >= 350 && !GuiEvent.CustomTermGuiClick(slot, button).postAndCatch() && it.canClick(slot, button)) {
                    it.click(slot, if (button == 0) ClickType.Middle else ClickType.Right, hideClicked && !it.isClicked)
                    if (TerminalSolver.customAnimations) colorAnimations[slot]?.start()
                }
            }
        }
    }

    fun closeGui() {
        colorAnimations.clear()
    }

    open fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()

        renderTerminal(TerminalSolver.currentTerm?.type?.windowSize?.minus(10) ?: 0)
    }

    private fun getRowOffset(slotCount: Int): Float {
        return when (slotCount) {
            in 0..9 -> 0f
            in 10..18 -> 1f
            in 19..27 -> 2f
            in 28..36 -> 2f
            in 37..45 -> 2f
            else -> 3f
        }
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
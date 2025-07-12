package me.odinmain.features.impl.floor7.p3.termGUI

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.hideClicked
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.ui.animations.ColorAnimation
import me.odinmain.utils.ui.rendering.NVGRenderer

object CustomTermGui {
    fun render() {
        NVGRenderer.beginFrame(1920f, 1080f)
        TerminalSolver.currentTerm?.type?.gui?.render()
        NVGRenderer.endFrame()
    }

    fun mouseClicked(x: Int, y: Int, button: Int) = TerminalSolver.currentTerm?.type?.gui?.mouseClicked(x, y, button)
}

abstract class TermGui(val name: String) {
    private val titleWidth = NVGRenderer.textWidth(name, 30f * TerminalSolver.customTermSize, NVGRenderer.defaultFont)
    protected val itemIndexMap: MutableMap<Int, Box> = mutableMapOf()
    inline val currentSolution get() = TerminalSolver.currentTerm?.solution.orEmpty()
    protected val colorAnimations = mutableMapOf<Int, ColorAnimation>()

    abstract fun renderTerminal(slotCount: Int)

    protected fun renderBackground(slotCount: Int) {
        val slotSize = 55f * TerminalSolver.customTermSize
        val backgroundStartX = 1920f / 2f + (-4 * slotSize) - 25f * TerminalSolver.customTermSize - 75f * TerminalSolver.customTermSize
        val backgroundStartY = 1080f / 2f + (-getRowOffset(slotCount) * slotSize) - 25f * TerminalSolver.customTermSize - 25f * TerminalSolver.customTermSize
        val backgroundWidth = 9 * slotSize - 5f * TerminalSolver.customTermSize + 150f * TerminalSolver.customTermSize
        val backgroundHeight = ((slotCount + 9) / 9) * slotSize - 5f * TerminalSolver.customTermSize + 55f * TerminalSolver.customTermSize

        NVGRenderer.rect(backgroundStartX, backgroundStartY, backgroundWidth, backgroundHeight, Colors.gray26.rgba, 12f * TerminalSolver.customTermSize)
        NVGRenderer.text(name, 1920f / 2f - titleWidth / 2, backgroundStartY + (12.5f + 15f) * TerminalSolver.customTermSize, 30f * TerminalSolver.customTermSize, Colors.WHITE.rgba, NVGRenderer.defaultFont)
    }

    protected fun renderSlot(index: Int, startColor: Color, endColor: Color): Pair<Float, Float> {
        val slotSize = 55f * TerminalSolver.customTermSize
        val slotInnerSize = 50f * TerminalSolver.customTermSize
        val x = (index % 9 - 4) * slotSize + 1920f / 2f - slotInnerSize / 2
        val y = (index / 9 - 2) * slotSize + 1080f / 2f - slotInnerSize / 2
        itemIndexMap[index] = Box(x - 2.5f * TerminalSolver.customTermSize, y - 2.5f * TerminalSolver.customTermSize, slotInnerSize + 5f * TerminalSolver.customTermSize, slotInnerSize + 5f * TerminalSolver.customTermSize)

        val colorAnim = colorAnimations.getOrPut(index) { ColorAnimation(250) }

        NVGRenderer.rect(x, y, slotInnerSize, slotInnerSize, colorAnim.get(startColor, endColor, true).rgba, 9f * TerminalSolver.customTermSize)
        return x to y
    }

    fun mouseClicked(x: Int, y: Int, button: Int) {
        itemIndexMap.entries.find { it.value.isPointWithin(x, y) }?.let { (slot, _) ->
            TerminalSolver.currentTerm?.let {
                if (System.currentTimeMillis() - it.timeOpened >= 300 && !GuiEvent.CustomTermGuiClick(slot, button).postAndCatch() && it.canClick(slot, button)) {
                    it.click(slot, if (button == 0) ClickType.Middle else ClickType.Right, hideClicked && !it.isClicked)
                    colorAnimations[slot]?.start()
                }
            }
        }
    }

    open fun render() {
        setCurrentGui(this)
        itemIndexMap.clear()

        renderTerminal(TerminalSolver.currentTerm?.type?.windowSize?.minus(10) ?: 0)
    }

    private fun getRowOffset(slotCount: Int): Int {
        return when (slotCount) {
            in 0..9 -> 0
            in 10..18 -> 1
            in 19..27 -> 2
            in 28..36 -> 2
            in 37..45 -> 2
            else -> 3
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

    data class Box(val x: Float, val y: Float, val w: Float, val h: Float) {
        fun isPointWithin(px: Int, py: Int): Boolean {
            return px in (x.toInt()..(x + w).toInt()) && py in (y.toInt()..(y + h).toInt())
        }
    }
}
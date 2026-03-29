package com.odtheking.odin.features.impl.boss.termGUI

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.features.impl.boss.TerminalSolver.hideClicked
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import com.odtheking.odin.utils.ui.widget.CustomGUIImpl
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.abs

abstract class TermGui {
    protected data class SlotVisual(
        val resolve: () -> Pair<Color, String?>?,
        val onRenderContent: (GuiGraphicsExtractor.(Int, Int, Int, Int) -> Unit)? = null,
    )

    private data class SlotBox(
        val slotIndex: Int,
        val x: Int,
        val y: Int,
        val size: Int,
        val visual: SlotVisual,
    ) {
        fun contains(mouseX: Int, mouseY: Int): Boolean =
            mouseX >= x && mouseY >= y && mouseX < x + size && mouseY < y + size
    }

    private data class TermLayout(
        val backgroundX: Int,
        val backgroundY: Int,
        val width: Int,
        val height: Int,
        val slots: List<SlotBox>,
    )

    private data class LayoutKey(
        val screenWidth: Int,
        val screenHeight: Int,
        val scale: Float,
        val gap: Float,
    )

    inline val currentSolution get() = TerminalUtils.currentTerm?.solution.orEmpty()
    private var layout: TermLayout? = null
    private var layoutKey: LayoutKey? = null
    private var hoveredSlotIndex: Int? = null
    open fun buildTerminal(screen: AbstractContainerScreen<*>) {}

    init {
        CustomGUIImpl.register(
            CustomGUIImpl.HandlerSet(::isActiveTermScreen,
            render = fun ScreenEvent.Render.(): Any {
                val screen = currentTermScreen() ?: return false
                ensureLayout(screen)
                renderLayout(guiGraphics, mouseX, mouseY)
                return true
            },
            click = fun ScreenEvent.MouseClick.(): Any {
                val screen = currentTermScreen() ?: return false
                ensureLayout(screen)
                hoveredSlotIndex = layout?.slots?.firstOrNull { it.contains(click.x().toInt(), click.y().toInt()) }?.slotIndex
                hoveredSlotIndex?.let { customTerminalClick(it, click.button()) }
                return true
            },
            key = fun ScreenEvent.KeyPress.(): Any {
                if (!isTerminalOverrideKey(input)) return false
                hoveredSlotIndex?.let {
                    customTerminalClick(it, if (!input.hasControlDown()) GLFW.GLFW_MOUSE_BUTTON_1 else GLFW.GLFW_MOUSE_BUTTON_2)
                }
                return true
            })
        )
    }

    private fun currentTermScreen(): AbstractContainerScreen<*>? = mc.screen as? AbstractContainerScreen<*>

    private fun isActiveTermScreen(): Boolean {
        if (!TerminalSolver.customGuiEnabled) return false
        if (TerminalUtils.currentTerm == null) return false
        if (currentTermScreen() == null) return false
        return TerminalUtils.currentTerm?.type?.getGUI() === this
    }

    private fun isTerminalOverrideKey(event: KeyEvent): Boolean =
        mc.options.keyDrop.matches(event) || mc.options.keyHotbarSlots.any { it.matches(event) }

    private fun ensureLayout(screen: AbstractContainerScreen<*>) {
        val key = LayoutKey(screen.width, screen.height, TerminalSolver.customTermSize, TerminalSolver.gap.toFloat())
        if (layout != null && layoutKey == key) return

        layout = null
        layoutKey = key
        hoveredSlotIndex = null
        buildTerminal(screen)
    }

    protected fun buildTerminalGrid(
        screen: AbstractContainerScreen<*>,
        rows: Int, cols: Int, startRow: Int,
        startCol: Int, slotFactory: (index: Int) -> SlotVisual?
    ) {
        val scale = TerminalSolver.customTermSize
        val slotSize = (24f * scale).toInt()
        val gap = (TerminalSolver.gap * scale).toInt()
        val padding = (2f * scale).toInt()

        val totalSlotSpace = slotSize + gap
        val totalWidth = (cols * slotSize + (cols - 1) * gap) + padding * 2
        val totalHeight = (rows * slotSize + (rows - 1) * gap) + padding * 2

        val backgroundX = (screen.width - totalWidth) / 2
        val backgroundY = (screen.height - totalHeight) / 2
        val slotBoxes = mutableListOf<SlotBox>()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = (startRow + row) * 9 + (startCol + col)
                val visual = slotFactory(index) ?: continue
                slotBoxes += SlotBox(
                    slotIndex = index,
                    x = backgroundX + padding + col * totalSlotSpace,
                    y = backgroundY + padding + row * totalSlotSpace,
                    size = slotSize,
                    visual = visual,
                )
            }
        }

        layout = TermLayout(backgroundX, backgroundY, totalWidth, totalHeight, slotBoxes)
    }

    protected fun createSlotVisualFromRendering(slotIndex: Int): SlotVisual {
        return SlotVisual(resolve = { TerminalUtils.currentTerm?.getSlotRendering(slotIndex) }) { guiX, guiY, width, height ->
            TerminalUtils.currentTerm?.getSlotRendering(slotIndex)?.second
                ?.let { renderSlotText(it, guiX, guiY, width, height, Colors.WHITE) }
        }
    }

    fun customTerminalClick(slotIndex: Int, button: Int) {
        TerminalUtils.currentTerm?.let { term ->
            val currentScreen = mc.screen ?: return@let
            val button = if (button == 0) GLFW.GLFW_MOUSE_BUTTON_3 else button
            if (System.currentTimeMillis() - term.timeOpened >= TerminalSolver.firstClickProt &&
                !GuiEvent.CustomTermGuiClick(currentScreen, slotIndex, button).postAndCatch() &&
                term.canClick(slotIndex, button)
            ) term.click(slotIndex, button, hideClicked && !term.isClicked)
        }
    }

    private fun renderLayout(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val termLayout = layout ?: return
        hoveredSlotIndex = null

        guiGraphics.renderWithTerminalScale(termLayout.backgroundX, termLayout.backgroundY, termLayout.width, termLayout.height) { x, y, w, h ->
            val radius = TerminalSolver.roundness
            if (radius > 0) roundedFill(x, y, x + w, y + h, TerminalSolver.backgroundColor.rgba, radius)
            else fill(x, y, x + w, y + h, TerminalSolver.backgroundColor.rgba)
        }

        termLayout.slots.forEach { slot ->
            val renderData = slot.visual.resolve() ?: return@forEach
            if (slot.contains(mouseX, mouseY)) hoveredSlotIndex = slot.slotIndex
            guiGraphics.renderWithTerminalScale(slot.x, slot.y, slot.size, slot.size) { x, y, w, h ->
                val radius = TerminalSolver.roundness
                if (radius > 0) roundedFill(x, y, x + w, y + h, renderData.first.rgba, radius)
                else fill(x, y, x + w, y + h, renderData.first.rgba)
                slot.visual.onRenderContent?.invoke(this, x, y, w, h)
            }
        }
    }

    protected inline fun GuiGraphicsExtractor.renderWithTerminalScale(x: Int, y: Int, width: Int, height: Int, block: GuiGraphicsExtractor.(Int, Int, Int, Int) -> Unit) {
        val scale = TerminalSolver.customTermSize
        if (abs(scale) == 1f) return block(x, y, width, height)

        pose().pushMatrix()
        pose().scale(scale)
        block((x / scale).toInt(), (y / scale).toInt(), (width / scale).toInt(), (height / scale).toInt())
        pose().popMatrix()
    }

    protected fun GuiGraphicsExtractor.renderSlotText(text: String, x: Int, y: Int, width: Int, height: Int, color: Color) {
        val textX = x + (width  - mc.font.width(text)) / 2
        val textY = y + (height - mc.font.lineHeight) / 2 + 1

        text(text, textX, textY, color)
    }
}

fun simpleTermGui(rows: Int, cols: Int, startRow: Int, startCol: Int): TermGui =
    object : TermGui() {
        override fun buildTerminal(screen: AbstractContainerScreen<*>) {
            buildTerminalGrid(screen, rows, cols, startRow, startCol) { index ->
                createSlotVisualFromRendering(index)
            }
        }
    }

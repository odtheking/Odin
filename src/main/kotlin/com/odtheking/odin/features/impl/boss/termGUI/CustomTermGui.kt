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

// Not really pixel perfect but good enough
abstract class TermGui {
    protected data class SlotVisual(
        val resolve: () -> Pair<Color, String?>?,
        val onRenderContent: (GuiGraphicsExtractor.(Int, Int, Int, Int) -> Unit)? = null,
    )

    private data class SlotBox(val slotIndex: Int, val bx: Int, val by: Int, val size: Int, val visual: SlotVisual) {
        fun containsBase(bx2: Float, by2: Float) = bx2 >= bx && by2 >= by && bx2 < bx + size && by2 < by + size
    }

    private data class Grid(val originX: Float, val originY: Float, val w: Int, val h: Int, val slots: List<SlotBox>) {
        fun toBase(screenX: Double, screenY: Double, scale: Float) =
            Pair((screenX.toFloat() - originX) / scale, (screenY.toFloat() - originY) / scale)
    }

    inline val currentSolution get() = TerminalUtils.currentTerm?.solution.orEmpty()
    private var grid: Grid? = null
    private var hoveredSlotIndex: Int? = null
    protected open val guiScale get() = TerminalSolver.customTermSize
    open fun buildTerminal(screen: AbstractContainerScreen<*>) {}

    init {
        CustomGUIImpl.register(CustomGUIImpl.HandlerSet(::isActiveTermScreen,
            render = fun ScreenEvent.Render.(): Any {
                val screen = currentTermScreen() ?: return false
                buildTerminal(screen)
                render(guiGraphics, mouseX, mouseY)
                return true
            },
            click = fun ScreenEvent.MouseClick.(): Any {
                grid?.let { g ->
                    val (bx, by) = g.toBase(click.x(), click.y(), guiScale)
                    hoveredSlotIndex = g.slots.firstOrNull { it.containsBase(bx, by) }?.slotIndex
                    hoveredSlotIndex?.let { customTerminalClick(it, click.button()) }
                }; return true
            },
            key = fun ScreenEvent.KeyPress.(): Any {
                if (!isTerminalOverrideKey(input)) return false
                hoveredSlotIndex?.let {
                    customTerminalClick(it, if (!input.hasControlDown()) GLFW.GLFW_MOUSE_BUTTON_1 else GLFW.GLFW_MOUSE_BUTTON_2)
                }; return true
            })
        )
    }

    private fun currentTermScreen() = mc.screen as? AbstractContainerScreen<*>

    private fun isActiveTermScreen(): Boolean {
        if (!TerminalSolver.customGuiEnabled || TerminalUtils.currentTerm == null || currentTermScreen() == null) return false
        return TerminalUtils.currentTerm?.type?.getGUI() === this
    }

    private fun isTerminalOverrideKey(event: KeyEvent) =
        mc.options.keyDrop.matches(event) || mc.options.keyHotbarSlots.any { it.matches(event) }

    protected fun buildTerminalGrid(
        screen: AbstractContainerScreen<*>,
        rows: Int, cols: Int, startRow: Int, startCol: Int,
        slotFactory: (index: Int) -> SlotVisual?
    ) {
        val slotSize = 24
        val gap      = TerminalSolver.gap
        val scale    = guiScale
        val w        = cols * slotSize + (cols - 1) * gap
        val h        = rows * slotSize + (rows - 1) * gap
        grid = Grid(
            originX = (screen.width  - w * scale) / 2f,
            originY = (screen.height - h * scale) / 2f,
            w = w, h = h,
            slots = buildList {
                for (row in 0 until rows) for (col in 0 until cols) {
                    val index = (startRow + row) * 9 + (startCol + col)
                    val visual = slotFactory(index) ?: continue
                    add(SlotBox(index, col * (slotSize + gap), row * (slotSize + gap), slotSize, visual))
                }
            }
        )
    }

    protected fun createSlotVisualFromRendering(slotIndex: Int) = SlotVisual(
        resolve = { TerminalUtils.currentTerm?.getSlotRendering(slotIndex) }
    ) { x, y, w, h ->
        TerminalUtils.currentTerm?.getSlotRendering(slotIndex)?.second?.let { renderSlotText(it, x, y, w, h, Colors.WHITE) }
    }

    fun customTerminalClick(slotIndex: Int, button: Int) {
        TerminalUtils.currentTerm?.let { term ->
            val screen = mc.screen ?: return@let
            val btn = if (button == 0) GLFW.GLFW_MOUSE_BUTTON_3 else button
            if (System.currentTimeMillis() - term.timeOpened >= TerminalSolver.firstClickProt &&
                !GuiEvent.CustomTermGuiClick(screen, slotIndex, btn).postAndCatch() &&
                term.canClick(slotIndex, btn)
            ) term.click(slotIndex, btn, hideClicked && !term.isClicked)
        }
    }

    private fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val g       = grid ?: return
        val scale   = guiScale
        val padding = 2
        val radius  = TerminalSolver.roundness
        val (baseMX, baseMY) = g.toBase(mouseX.toDouble(), mouseY.toDouble(), scale)
        hoveredSlotIndex = null

        guiGraphics.pose().pushMatrix()
        guiGraphics.pose().translate(g.originX, g.originY)
        guiGraphics.pose().scale(scale)

        guiGraphics.roundedFill(-padding, -padding, g.w + padding, g.h + padding, TerminalSolver.backgroundColor.rgba, radius)

        g.slots.forEach { slot ->
            val (color, _) = slot.visual.resolve() ?: return@forEach
            if (slot.containsBase(baseMX, baseMY)) hoveredSlotIndex = slot.slotIndex
            guiGraphics.roundedFill(slot.bx, slot.by, slot.bx + slot.size, slot.by + slot.size, color.rgba, radius)
            slot.visual.onRenderContent?.invoke(guiGraphics, slot.bx, slot.by, slot.size, slot.size)
        }

        guiGraphics.pose().popMatrix()
    }

    protected fun GuiGraphics.renderSlotText(text: String, x: Int, y: Int, width: Int, height: Int, color: Color) =
        text(text, x + (width - mc.font.width(text)) / 2, y + (height - mc.font.lineHeight) / 2 + 1, color)
}

fun simpleTermGui(rows: Int, cols: Int, startRow: Int, startCol: Int): TermGui =
    object : TermGui() {
        override fun buildTerminal(screen: AbstractContainerScreen<*>) =
            buildTerminalGrid(screen, rows, cols, startRow, startCol) { createSlotVisualFromRendering(it) }
    }
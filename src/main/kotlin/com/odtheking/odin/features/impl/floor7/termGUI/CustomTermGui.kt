package com.odtheking.odin.features.impl.floor7.termGUI

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.features.impl.floor7.TerminalSolver
import com.odtheking.odin.features.impl.floor7.TerminalSolver.hideClicked
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import com.odtheking.odin.utils.ui.widget.CustomGUIImpl
import com.odtheking.odin.utils.ui.widget.SimpleWidget
import com.odtheking.odin.utils.ui.widget.simpleWidget
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.lwjgl.glfw.GLFW
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.roundToInt

abstract class TermGui {
    inline val currentSolution get() = TerminalUtils.currentTerm?.solution.orEmpty()

    protected val slotWidgets = ConcurrentHashMap<Int, SimpleWidget>()

    open fun buildTerminal(screen: AbstractContainerScreen<*>) {}

    open fun build() {
        val screen = mc.screen as? AbstractContainerScreen<*> ?: return

        slotWidgets.clear()
        CustomGUIImpl.clear(screen)
        buildTerminal(screen)
    }

    protected fun buildTerminalGrid(
        screen: AbstractContainerScreen<*>,
        rows: Int, cols: Int, startRow: Int,
        startCol: Int, slotFactory: (index: Int) -> SimpleWidget?
    ) {
        val scale = TerminalSolver.customTermSize
        val slotSize = (24f * scale).roundToInt()
        val gap = (TerminalSolver.gap * scale).roundToInt()
        val padding = (2f * scale).roundToInt()

        val totalSlotSpace = slotSize + gap
        val totalWidth = (cols * slotSize + (cols - 1) * gap) + padding * 2
        val totalHeight = (rows * slotSize + (rows - 1) * gap) + padding * 2

        val backgroundX = (screen.width - totalWidth) / 2
        val backgroundY = (screen.height - totalHeight) / 2

        CustomGUIImpl.register(screen, createBackgroundWidget(backgroundX, backgroundY, totalWidth, totalHeight))

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = (startRow + row) * 9 + (startCol + col)
                val widget = slotFactory(index) ?: createSlotWidget(index, Colors.TRANSPARENT).apply { visible = false }

                widget.x = backgroundX + padding + col * totalSlotSpace
                widget.y = backgroundY + padding + row * totalSlotSpace
                CustomGUIImpl.register(screen, widget)
            }
        }
    }

    protected fun createSlotWidgetFromRendering(slotIndex: Int): SimpleWidget? {
        val (color, text) = TerminalUtils.currentTerm?.getSlotRendering(slotIndex) ?: return null
        return createSlotWidget(slotIndex, color) { guiX, guiY, width, height ->
            text?.let { renderSlotText(it, guiX, guiY, width, height, Colors.WHITE) }
        }
    }

    protected fun createSlotWidget(slotIndex: Int, color: Color, onRenderContent: (GuiGraphics.(Int, Int, Int, Int) -> Unit)? = null): SimpleWidget {
        val slotSize = (24f * TerminalSolver.customTermSize).roundToInt().coerceAtLeast(1)

        return simpleWidget(width = slotSize, height = slotSize) {
            onClick { event, _ ->
                customTerminalClick(slotIndex, event.button())
                true
            }
            onKeyPress { event ->
                if ((mc.options.keyDrop.matches(event) || mc.options.keyHotbarSlots.any { it.matches(event) }) && isHovered)
                    customTerminalClick(slotIndex, if (!event.hasControlDown()) GLFW.GLFW_MOUSE_BUTTON_1 else GLFW.GLFW_MOUSE_BUTTON_2)
                false
            }
            onRender { guiX, guiY, width, height ->
                renderWithTerminalScale(guiX, guiY, width, height) { scaledX, scaledY, scaledW, scaledH ->
                    val radius = TerminalSolver.roundness
                    if (radius > 0) roundedFill(scaledX, scaledY, scaledX + scaledW, scaledY + scaledH, color.rgba, radius)
                    else this.fill(scaledX, scaledY, scaledX + scaledW, scaledY + scaledH, color.rgba)
                    onRenderContent?.invoke(this, scaledX, scaledY, scaledW, scaledH)
                }
            }
        }.apply {
            slotWidgets[slotIndex] = this
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

    protected fun createBackgroundWidget(x: Int, y: Int, width: Int, height: Int): SimpleWidget {
        return simpleWidget(x, y, width, height) {
            onRender { guiX, guiY, w, h ->
                renderWithTerminalScale(guiX, guiY, w, h) { scaledX, scaledY, scaledW, scaledH ->
                    val radius = TerminalSolver.roundness
                    if (radius > 0) roundedFill(scaledX, scaledY, scaledX + scaledW, scaledY + scaledH, TerminalSolver.backgroundColor.rgba, radius)
                    else this.fill(scaledX, scaledY, scaledX + scaledW, scaledY + scaledH, TerminalSolver.backgroundColor.rgba)
                }
            }
        }.apply { this.active = false }
    }

    protected inline fun GuiGraphics.renderWithTerminalScale(x: Int, y: Int, width: Int, height: Int, block: GuiGraphics.(Int, Int, Int, Int) -> Unit) {
        val scale = TerminalSolver.customTermSize
        if (abs(scale) == 1f) return block(x, y, width, height)

        pose().pushMatrix()
        pose().scale(scale)
        block((x / scale).roundToInt(), (y / scale).roundToInt(), (width / scale).roundToInt(), (height / scale).roundToInt())
        pose().popMatrix()
    }

    protected fun GuiGraphics.renderSlotText(text: String, x: Int, y: Int, width: Int, height: Int, color: Color) {
        val textX = x + (width -  mc.font.width(text)) / 2
        val textY = y + (height - mc.font.lineHeight) / 2 + 1

        this.text(text, textX, textY, color)
    }
}

fun simpleTermGui(rows: Int, cols: Int, startRow: Int, startCol: Int): TermGui =
    object : TermGui() {
        override fun buildTerminal(screen: AbstractContainerScreen<*>) {
            buildTerminalGrid(screen, rows, cols, startRow, startCol) { index ->
                createSlotWidgetFromRendering(index)
            }
        }
    }

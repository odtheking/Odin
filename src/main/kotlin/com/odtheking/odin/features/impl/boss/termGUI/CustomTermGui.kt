package com.odtheking.odin.features.impl.boss.termGUI

import androidx.compose.runtime.Composable
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import com.odtheking.odin.utils.ui.compose.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.roundToInt

open class TermGui(val rows: Int, val cols: Int, private val startRow: Int, private val startCol: Int) {
    open val scale: Float get() = TerminalSolver.customTermSize

    open val textColor: Color
        get() = if (TerminalUtils.currentTerm?.type == TerminalTypes.NUMBERS) TerminalSolver.numbersText else TerminalSolver.rubixText

    open fun slotRendering(index: Int): Pair<Color, String?>? = TerminalUtils.currentTerm?.getSlotRendering(index)

    fun slotIndex(row: Int, col: Int): Int = (startRow + row) * 9 + startCol + col
}

@Composable
fun TermGrid(gui: TermGui): UiNode =
    Box {
        repeat(gui.rows) { row ->
            repeat(gui.cols) { col ->
                TermSlot(gui, gui.slotIndex(row, col)).offset({ col * gui.pitch }, { row * gui.pitch })
            }
        }
    }
        .width { gui.span(gui.cols) }
        .height { gui.span(gui.rows) }
        .centeredOnFramebuffer()
        .drawBehind { graphics ->
            val padding = gui.pixels(PADDING)
            graphics.roundedRect(x - padding, y - padding, right + padding, bottom + padding, TerminalSolver.backgroundColor.rgba, gui.radius)
        }

@Composable
private fun TermSlot(gui: TermGui, index: Int): UiNode =
    Canvas { graphics ->
        val (color, text) = gui.slotRendering(index) ?: return@Canvas
        graphics.roundedRect(x, y, right, bottom, color.rgba, gui.radius)
        if (text != null) graphics.slotText(text, this, gui.fontScale, gui.textColor.rgba)
    }
        .width { gui.slotSize }
        .height { gui.slotSize }
        .tag(index)
        .pointerInput { event ->
            TerminalUtils.currentTerm?.clickMouse(index, event.button(), TerminalSolver.clickPrediction)
            true
        }

private fun UiNode.centeredOnFramebuffer() = offset({ (mc.window.width - width) / 2 }, { (mc.window.height - height) / 2 })

private fun GuiGraphicsExtractor.slotText(text: String, slot: UiNode, scale: Int, color: Int) {
    pose().pushMatrix()
    pose().translate(
        (slot.x + (slot.width - mc.font.width(text) * scale) / 2).toFloat(),
        (slot.y + (slot.height - TEXT_HEIGHT * scale) / 2).toFloat(),
    )
    pose().scale(scale.toFloat())
    text(mc.font, text, 0, 0, color, true)
    pose().popMatrix()
}

private val TermGui.pixelsPerUnit: Float get() = scale * mc.window.guiScale
private fun TermGui.pixels(units: Int): Int = (units * pixelsPerUnit).roundToInt()
private val TermGui.slotSize: Int get() = pixels(SLOT_SIZE)
private val TermGui.gap: Int get() = pixels(TerminalSolver.gap)
private val TermGui.pitch: Int get() = slotSize + gap
private fun TermGui.span(count: Int): Int = count * pitch - gap
private val TermGui.radius: Float get() = TerminalSolver.roundness * pixelsPerUnit
private val TermGui.fontScale: Int get() = pixelsPerUnit.roundToInt().coerceAtLeast(1)

private const val SLOT_SIZE = 24
private const val PADDING = 2
private const val TEXT_HEIGHT = 8
package com.odtheking.odin.features.impl.boss.termGUI

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import com.odtheking.odin.utils.ui.widget.CustomGUIImpl
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.lwjgl.glfw.GLFW

object FancyTermGui {

    private const val SLOT_SIZE = 24f
    private const val SLOT_GAP = 2f
    private const val PADDING = 12

    private data class SlotBox(
        val slotIndex: Int,
        val bx: Float,
        val by: Float,
        val size: Float,
        val getVisual: () -> Pair<Color, String?>?
    ) {
        fun contains(x: Float, y: Float) = x >= bx && y >= by && x < bx + size && y < by + size
    }

    private data class Grid(
        val originX: Float,
        val originY: Float,
        val w: Float,
        val h: Float,
        val slots: List<SlotBox>
    ) {
        fun toBase(screenX: Double, screenY: Double, scale: Float) =
            Pair((screenX.toFloat() - originX) / scale, (screenY.toFloat() - originY) / scale)
    }

    private var currentGrid: Grid? = null
    private var hoveredSlotIndex: Int? = null

    @JvmStatic
    fun isActive(): Boolean {
        return TerminalSolver.fancyGuiEnabled &&
                TerminalUtils.currentTerm != null &&
                mc.screen is AbstractContainerScreen<*>
    }

    private data class GridLayout(val rows: Int, val cols: Int, val startRow: Int, val startCol: Int)

    private fun getGridLayout(type: TerminalTypes): GridLayout = when (type) {
        TerminalTypes.PANES -> GridLayout(3, 5, 1, 2)
        TerminalTypes.RUBIX -> GridLayout(3, 3, 1, 3)
        TerminalTypes.NUMBERS -> GridLayout(2, 7, 1, 1)
        TerminalTypes.STARTS_WITH -> GridLayout(3, 7, 1, 1)
        TerminalTypes.SELECT -> GridLayout(4, 7, 1, 1)
        TerminalTypes.MELODY -> GridLayout(5, 7, 0, 1)
    }

    private fun buildGrid(screen: AbstractContainerScreen<*>) {
        val term = TerminalUtils.currentTerm ?: return
        val type = term.type
        val scale = TerminalSolver.fancyScale
        val layout = getGridLayout(type)

        val (rows, cols, startRow, startCol) = layout
        val w = cols * SLOT_SIZE + (cols - 1) * SLOT_GAP
        val h = rows * SLOT_SIZE + (rows - 1) * SLOT_GAP

        val slots = buildList {
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val index = (startRow + row) * 9 + (startCol + col)
                    val r = index / 9
                    val c = index % 9

                    val getVisual: () -> Pair<Color, String?>? = when (type) {
                        TerminalTypes.MELODY -> ({
                            val solution = TerminalUtils.currentTerm?.solution
                            val inSolution = solution?.contains(index) == true
                            val color: Color? = when {
                                r == 0 -> if (inSolution) TerminalSolver.melodyColumColor else null
                                c == 7 -> if (inSolution) TerminalSolver.melodyPointerColor else TerminalSolver.melodyBackgroundColor
                                c in 1..5 -> if (inSolution) TerminalSolver.melodyPointerColor else TerminalSolver.melodyBackgroundColor
                                else -> null
                            }
                            color?.let { it to null }
                        })
                        else -> ({ TerminalUtils.currentTerm?.getSlotRendering(index) })
                    }

                    if (type == TerminalTypes.MELODY) {
                        if (r == 0 || (c == 7 && r in 1..4) || c in 1..5) {
                            add(SlotBox(index, col * (SLOT_SIZE + SLOT_GAP), row * (SLOT_SIZE + SLOT_GAP), SLOT_SIZE, getVisual))
                        }
                    } else {
                        add(SlotBox(index, col * (SLOT_SIZE + SLOT_GAP), row * (SLOT_SIZE + SLOT_GAP), SLOT_SIZE, getVisual))
                    }
                }
            }
        }

        currentGrid = Grid(
            originX = (screen.width - w * scale) / 2f,
            originY = (screen.height - h * scale) / 2f,
            w = w, h = h,
            slots = slots
        )
    }

    @JvmStatic
    fun handleClick(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isActive()) return false
        val g = currentGrid ?: return true
        val scale = TerminalSolver.fancyScale
        val (bx, by) = g.toBase(mouseX, mouseY, scale)

        val slot = g.slots.firstOrNull { it.contains(bx, by) } ?: return true

        val term = TerminalUtils.currentTerm ?: return true
        val screen = mc.screen ?: return true
        val btn = if (button == 0) GLFW.GLFW_MOUSE_BUTTON_3 else button

        if (System.currentTimeMillis() - term.timeOpened >= TerminalSolver.firstClickProt &&
            !GuiEvent.CustomTermGuiClick(screen, slot.slotIndex, btn).postAndCatch() &&
            term.canClick(slot.slotIndex, btn)
        ) {
            term.click(slot.slotIndex, btn, TerminalSolver.hideClicked && !term.isClicked)
        }
        return true
    }

    init {
        on<GuiEvent.SlotClick> {
            if (isActive()) cancel()
        }

        CustomGUIImpl.register(CustomGUIImpl.HandlerSet(
            enabled = ::isActive,
            render = fun ScreenEvent.Render.(): Any {
                val screen = mc.screen as? AbstractContainerScreen<*> ?: return false
                buildGrid(screen)
                renderFancy(guiGraphics, mouseX, mouseY)
                return true
            },
            click = fun ScreenEvent.MouseClick.(): Any {
                handleClick(click.x(), click.y(), click.button())
                return true
            }
        ))
    }

    private fun renderFancy(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val g = currentGrid ?: return
        val scale = TerminalSolver.fancyScale
        val guiBgColor = TerminalSolver.backgroundColor
        val guiRadius = TerminalSolver.fancyGuiBorderRadius
        val btnRadius = TerminalSolver.fancyButtonBorderRadius

        val (baseMX, baseMY) = g.toBase(mouseX.toDouble(), mouseY.toDouble(), scale)
        hoveredSlotIndex = null

        guiGraphics.pose().pushMatrix()
        guiGraphics.pose().translate(g.originX, g.originY)
        guiGraphics.pose().scale(scale)

        val accentColor = guiBgColor.brighter(1.8f)
        guiGraphics.roundedFill(
            -PADDING, -PADDING,
            g.w.toInt() + PADDING, g.h.toInt() + PADDING,
            guiBgColor.rgba, guiRadius,
            accentColor.rgba, 1.5f
        )

        g.slots.forEach { slot ->
            val (color, text) = slot.getVisual() ?: return@forEach
            val isHovered = slot.contains(baseMX, baseMY)
            if (isHovered) hoveredSlotIndex = slot.slotIndex

            val displayColor = if (isHovered) color.brighter(1.3f) else color
            val bx = slot.bx.toInt()
            val by = slot.by.toInt()
            val sz = slot.size.toInt()

            guiGraphics.roundedFill(bx, by, bx + sz, by + sz, displayColor.rgba, btnRadius)

            val highlightAlpha = (displayColor.alphaFloat * 0.25f).coerceIn(0f, 1f)
            val highlightColor = Color(
                minOf(255, displayColor.red + 50),
                minOf(255, displayColor.green + 50),
                minOf(255, displayColor.blue + 50),
                highlightAlpha
            )
            if (sz > 4) {
                guiGraphics.roundedFill(bx + 1, by + 1, bx + sz - 1, by + sz / 3, highlightColor.rgba, btnRadius)
            }

            text?.let {
                val tx = bx + (sz - mc.font.width(it)) / 2
                val ty = by + (sz - mc.font.lineHeight) / 2 + 1
                guiGraphics.text(it, tx, ty, Colors.WHITE)
            }
        }

        guiGraphics.pose().popMatrix()
    }
}

package com.odtheking.odin.features.impl.boss

import com.odtheking.mixin.accessors.AbstractContainerScreenAccessor
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution for terminals in floor 7."
) {
    private val renderType by SelectorSetting("Render mode", "Normal", arrayListOf("Normal", "Custom GUI"), desc = "How the terminal solver should render.")
    val customTermSize by NumberSetting("Term Size", 2f, 1f, 3f, 0.1f, desc = "The size of the custom terminal GUI.").withDependency { renderType == 1 }
    private val normalTermSize by NumberSetting("Normal Term Size", 3, 1, 5, 1, desc = "The GUI scale increase for normal terminal GUI.").withDependency { renderType != 1 }
    val roundness by NumberSetting("Roundness", 6, 0f, 15f, 1f, desc = "The roundness of the custom terminal gui.").withDependency { renderType == 1 }
    val gap by NumberSetting("Gap", 2, 0f, 8, 1f, desc = "The gap between the slots in the custom terminal gui.").withDependency { renderType == 1 }

    private val solverSettings by DropdownSetting("Solver Functionality")
    private val cancelToolTip by BooleanSetting("Stop Tooltips", true, desc = "Stops rendering tooltips in terminals.").withDependency { renderType == 0 && solverSettings }
    private val middleClickGUI by BooleanSetting("Middle Click GUI", true, desc = "Replaces right click with middle click in terminals.").withDependency { renderType == 0 && solverSettings }
    private val blockIncorrectClicks by BooleanSetting("Block Incorrect Clicks", true, desc = "Blocks incorrect clicks in terminals.").withDependency { renderType == 0 && solverSettings }
    private val cancelMelodySolver by BooleanSetting("Stop Melody Solver", false, desc = "Stops rendering the melody solver.").withDependency { solverSettings }
    val showNumbers by BooleanSetting("Show Numbers", true, desc = "Shows numbers in the order terminal.").withDependency { solverSettings }
    val firstClickProt by NumberSetting("First Click Protection", 350, 350, 700, 10, unit = "ms", desc = "The amount of time after opening a terminal where clicks are blocked to prevent bans (recommended value is 500 minus your ping).").withDependency { blockIncorrectClicks && solverSettings }
    val hideClicked by BooleanSetting("Hide Clicked", false, desc = "Visually hides your first click before a gui updates instantly to improve perceived response time. Does not affect actual click time.").withDependency { solverSettings }
    val terminalReloadThreshold by NumberSetting("Solution resolve timeout", 600, 300, 1000, 10, unit = "ms", desc = "The amount of time before the terminal reloads after a click wasn't registered while using hide clicked.").withDependency { hideClicked && solverSettings }
    private val debug by BooleanSetting("Debug", false, desc = "Shows debug terminals.").withDependency { solverSettings }

    private val showColors by DropdownSetting("Color Settings")
    val backgroundColor by ColorSetting("Background", Colors.gray26, true, desc = "Background color of the terminal solver.").withDependency { showColors }

    val panesColor by ColorSetting("Panes", Colors.MINECRAFT_GREEN, true, desc = "Color of the panes terminal solver.").withDependency { showColors }

    val rubixColor1 by ColorSetting("Rubix 1", Colors.MINECRAFT_GREEN, true, desc = "Color of the rubix terminal solver for 1 click.").withDependency { showColors }
    val rubixColor2 by ColorSetting("Rubix 2", Colors.MINECRAFT_GREEN.darker(0.5f), true, desc = "Color of the rubix terminal solver for 2 click.").withDependency { showColors }
    val oppositeRubixColor1 by ColorSetting("Rubix -1", Colors.MINECRAFT_DARK_RED, true, desc = "Color of the rubix terminal solver for -1 click.").withDependency { showColors }
    val oppositeRubixColor2 by ColorSetting("Rubix -2", Colors.MINECRAFT_DARK_RED.darker(0.5f), true, desc = "Color of the rubix terminal solver for -2 click.").withDependency { showColors }

    val orderColor by ColorSetting("Order 1", Colors.MINECRAFT_GREEN, true, desc = "Color of the order terminal solver for 1st item.").withDependency { showColors }
    val orderColor2 by ColorSetting("Order 2", Colors.MINECRAFT_GREEN.darker(0.5f), true, desc = "Color of the order terminal solver for 2nd item.").withDependency { showColors }
    val orderColor3 by ColorSetting("Order 3", Colors.MINECRAFT_GREEN.darker(0.5f).darker(0.5f), true, desc = "Color of the order terminal solver for 3rd item.").withDependency { showColors }

    val startsWithColor by ColorSetting("Starts With", Colors.MINECRAFT_GREEN, true, desc = "Color of the starts with terminal solver.").withDependency { showColors }

    val selectColor by ColorSetting("Select", Colors.MINECRAFT_GREEN, true, desc = "Color of the select terminal solver.").withDependency { showColors }

    val melodyColumColor by ColorSetting("Melody Column", Colors.MINECRAFT_DARK_PURPLE, true, desc = "Color of the colum indicator for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyPointerColor by ColorSetting("Melody Pointer", Colors.MINECRAFT_GREEN, true, desc = "Color of the location for pressing for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyBackgroundColor by ColorSetting("Melody Background", Colors.gray38, true, desc = "Color of the background slot in melody.").withDependency { showColors && !cancelMelodySolver }

    @JvmStatic val termSize get() = if (enabled && renderType == 0 && TerminalUtils.currentTerm != null) normalTermSize else 1
    val customGuiEnabled get() = enabled && renderType == 1 && renderMelody
    private val renderMelody get() = !(cancelMelodySolver && TerminalUtils.currentTerm?.type == TerminalTypes.MELODY)

    init {
        on<GuiEvent.SlotClick> (EventPriority.HIGH) {
            val term = TerminalUtils.currentTerm ?: return@on

            if (
                System.currentTimeMillis() - term.timeOpened < firstClickProt ||
                (blockIncorrectClicks && !term.canClick(slotId, button))
            ) return@on cancel()

            if (middleClickGUI) {
                term.click(slotId, if (button == 0) GLFW.GLFW_MOUSE_BUTTON_3 else button, hideClicked && !term.isClicked)
                return@on cancel()
            }

            if (hideClicked && !term.isClicked) term.simulateClick(slotId, button)
        }

        on<GuiEvent.Render> {
            if (TerminalUtils.currentTerm == null || !renderMelody || renderType != 0) return@on

            val screen = (screen as? AbstractContainerScreen<*>) as? AbstractContainerScreenAccessor ?: return@on
            guiGraphics.fill(screen.x + 7, screen.y + 16, screen.x + screen.width - 7, screen.y + screen.height - 96, backgroundColor.rgba)
        }

        on<GuiEvent.RenderSlot> {
            if (!renderMelody) return@on
            val currentTerm = TerminalUtils.currentTerm ?: return@on

            if (slot.index <= currentTerm.type.windowSize - 1) {
                currentTerm.getSlotRendering(slot.index)?.let { (color, text) ->
                    guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.rgba)
                    text?.let { guiGraphics.drawCenteredString(screen.font, it, slot.x + 8, slot.y + 4, Colors.WHITE.rgba) }
                    cancel()
                }
            }
        }

        on<GuiEvent.DrawTooltip> {
            if (cancelToolTip && TerminalUtils.currentTerm != null) cancel()
        }

        on<TerminalEvent.Open> {
            if (renderType == 0) mc.execute { mc.resizeDisplay() }
        }

        on<TerminalEvent.Close> {
            if (renderType == 0) mc.execute { mc.resizeDisplay() }
        }

        on<ScreenEvent.Render> {
            if (debug) TerminalUtils.currentTerm?.let { term ->
                val menu = (mc.screen as? AbstractContainerScreen<*>)?.menu ?: return@let
                val debugInfo = listOf(
                    "§7Type: §f${term.type.name}",
                    "§7Window Name: §f${mc.screen?.title?.string}",
                    "§7Container ID: §f${menu.containerId}",
                    "§7Time Open: §f${System.currentTimeMillis() - term.timeOpened}ms",
                    "§7Is Clicked: §f${term.isClicked}",
                    "§7Window Count: §f${term.windowCount}",
                    "§7Solution: §f${term.solution.joinToString(", ")}",
                )

                guiGraphics.pose().pushMatrix()
                val sf = mc.window.guiScale
                guiGraphics.pose().scale(1f / sf, 1f / sf)
                debugInfo.forEachIndexed { index, line ->
                    guiGraphics.drawWordWrap(mc.font, Component.literal(line), 5, 20 + (index * 10), 300, Colors.WHITE.rgba)
                }

                menu.items.forEachIndexed { index, stack ->
                    guiGraphics.renderItem(stack, 5 + (index % 9) * 18, 250 + (index / 9) * 18)
                    guiGraphics.renderItemDecorations(mc.font, stack, 5 + (index % 9) * 18, 250 + (index / 9) * 18)
                }
                guiGraphics.pose().popMatrix()
            }
        }
    }
}
package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.client.Options
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Items

object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution for terminals in floor 7."
) {
    private val renderType by SelectorSetting("Render type", RenderType.ODIN, desc = "How the terminal solver should render.")
    private val normalTermSize by NumberSetting("Normal Term Size", 3, 1..6, 1, desc = "The GUI scale increase for normal terminal GUI.").withDependency { renderType == RenderType.ODIN || renderType == RenderType.NORMAL }
    val customTermSize by NumberSetting("Term Size", 2f, 1.0..3.0, 0.1, desc = "The size of the custom terminal GUI.").withDependency { renderType == RenderType.CUSTOM_GUI }
    val roundness by NumberSetting("Roundness", 5f, 0.0..15.0, 1.0, desc = "The roundness of the custom terminal gui.").withDependency { renderType == RenderType.CUSTOM_GUI }
    val gap by NumberSetting("Slot gap", 2, 0..8, 1, desc = "The gap between the slots in the custom terminal gui.").withDependency { renderType == RenderType.CUSTOM_GUI }

    private val solverSettings by DropdownSetting("Solver Functionality", desc = "Options related to how the solver behaves.")
    val clickPrediction by BooleanSetting("Client Prediction", true, desc = "Visually predicts the server state before the gui update is sent to the client.").withDependency { solverSettings }
    val terminalReloadThreshold by NumberSetting("Resolve timeout", 600, 300..1200, 10, unit = "ms", desc = "The amount of time before the terminal reloads after a click wasn't registered while using click prediction.").withDependency { clickPrediction && solverSettings }
    private val cancelMelodySolver by BooleanSetting("Stop Melody Solver", false, desc = "Stops rendering the melody solver.").withDependency { solverSettings }
    val melodyTermSize by NumberSetting("Melody Size", 1.5f, 1.0..3.0, 0.1, desc = "The size of the melody terminal GUI.").withDependency { !cancelMelodySolver && solverSettings && renderType == RenderType.CUSTOM_GUI }
    val rubixMode by SelectorSetting("Rubix Mode", RubixMode.FEWEST_CLICKS, desc = "Whether the rubix solver should mix in right clicks for the fewest clicks overall, or stick to left clicks only.").withDependency { solverSettings }

    private val firstClickProtSettings by DropdownSetting("First Click Prot Dropdown", desc = "Options related to first click protection.")
    val firstClickProt by NumberSetting("First Click Prot", 500, 350..800, 10, unit = "ms", desc = "The amount of time after opening a terminal where clicks are blocked to prevent bans (recommended value is 500 minus your ping).").withDependency { firstClickProtSettings }
    val shouldFirstClickProtWithTicks by BooleanSetting("Account For Server Lag", false, desc = "Prevents bans from clicking when the server lags after opening the terminal (disabled in singleplayer").withDependency { firstClickProtSettings }
    val firstClickProtTicks by NumberSetting("Lag Protection Ticks", 8, 7..16, unit = "ticks", desc = "Each tick = 50ms (recommended value is 8)").withDependency { shouldFirstClickProtWithTicks && firstClickProtSettings }

    private val showColors by DropdownSetting("Color Settings", desc = "Color options for the terminal solver.")
    val backgroundColor by ColorSetting("Background", Colors.gray26.withAlpha(0.5f), true, desc = "Background color of the terminal solver.").withDependency { showColors }

    val panesColor by ColorSetting("Panes", Colors.MINECRAFT_GREEN, true, desc = "Color of the panes terminal solver.").withDependency { showColors }

    val rubixText by ColorSetting("Rubix Text", Colors.WHITE, true, desc = "Text color of the Rubix terminal solver.").withDependency { showColors }
    val rubixColor1 by ColorSetting("Rubix 1", Colors.MINECRAFT_GREEN, true, desc = "Color of the rubix terminal solver for 1 click.").withDependency { showColors }
    val rubixColor2 by ColorSetting("Rubix 2", Colors.MINECRAFT_GREEN.darker(0.5f), true, desc = "Color of the rubix terminal solver for 2 click.").withDependency { showColors }
    val oppositeRubixColor1 by ColorSetting("Rubix -1", Colors.MINECRAFT_DARK_RED, true, desc = "Color of the rubix terminal solver for -1 click.").withDependency { showColors }
    val oppositeRubixColor2 by ColorSetting("Rubix -2", Colors.MINECRAFT_DARK_RED.darker(0.5f), true, desc = "Color of the rubix terminal solver for -2 click.").withDependency { showColors }

    val numbersText by ColorSetting("Numbers Text", Colors.WHITE, true, desc = "Text color of the Numbers terminal solver.").withDependency { showColors }
    val numbers1Color by ColorSetting("Numbers 1", Colors.MINECRAFT_GREEN, true, desc = "Color of the order terminal solver for 1st item.").withDependency { showColors }
    val numbers2Color by ColorSetting("Numbers 2", Colors.MINECRAFT_GREEN.darker(0.5f), true, desc = "Color of the order terminal solver for 2nd item.").withDependency { showColors }
    val numbers3Color by ColorSetting("Numbers 3", Colors.MINECRAFT_GREEN.darker(0.5f).darker(0.5f), true, desc = "Color of the order terminal solver for 3rd item.").withDependency { showColors }

    val startsWithColor by ColorSetting("Starts With", Colors.MINECRAFT_GREEN, true, desc = "Color of the starts with terminal solver.").withDependency { showColors }

    val selectColor by ColorSetting("Select", Colors.MINECRAFT_GREEN, true, desc = "Color of the select terminal solver.").withDependency { showColors }

    val melodyColumColor by ColorSetting("Melody Column", Colors.MINECRAFT_DARK_PURPLE, true, desc = "Color of the column indicator for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyPointerColor by ColorSetting("Melody Pointer", Colors.MINECRAFT_GREEN, true, desc = "Color of the location for pressing for melody.").withDependency { showColors && !cancelMelodySolver }
    val melodyBackgroundColor by ColorSetting("Melody Background", Colors.gray38, true, desc = "Color of the background slot in melody.").withDependency { showColors && !cancelMelodySolver }
    private val debug by BooleanSetting("Debug", false, desc = "Shows debug terminals.").withDependency { showColors }

    @JvmStatic val termSize get() = if (enabled && (renderType != RenderType.CUSTOM_GUI) && TerminalUtils.currentTerm != null) if (normalTermSize == 6) Options.AUTO_GUI_SCALE else normalTermSize else 1
    val customGuiEnabled get() = enabled && renderType == RenderType.CUSTOM_GUI && renderMelody
    private val renderMelody get() = !(cancelMelodySolver && TerminalUtils.currentTerm?.type == TerminalTypes.MELODY)

    init {
        on<GuiEvent.SlotClick> {
            TerminalUtils.currentTerm?.let {
                it.click(slotIndex, button, clickPrediction)
                cancel()
            }
        }

        on<GuiEvent.Render> {
            if (TerminalUtils.currentTerm == null || !renderMelody || renderType != RenderType.ODIN) return@on

            val screen = screen as? AbstractContainerScreen<*> ?: return@on
            guiGraphics.fill(screen.leftPos + 7, screen.topPos + 16, screen.leftPos + screen.imageWidth - 7, screen.topPos + screen.imageHeight - 96, backgroundColor.rgba)
        }

        on<GuiEvent.RenderSlot> {
            if (!renderMelody) return@on
            val currentTerm = TerminalUtils.currentTerm ?: return@on

            if (slot.index <= currentTerm.type.windowSize - 1) {
                currentTerm.getSlotRendering(slot.index)?.let { (color, text) ->
                    guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.rgba)
                    text?.let { guiGraphics.centeredText(screen.font, it, slot.x + 8, slot.y + 4, if (currentTerm.type == TerminalTypes.NUMBERS) numbersText.rgba else rubixText.rgba) }
                    cancel()
                }
                if (renderType == RenderType.ODIN) cancel()
            }
        }

        on<GuiEvent.DrawTooltip> {
            if (TerminalUtils.currentTerm != null) cancel()
            this.guiGraphics.renderDebug()
        }

        on<TerminalEvent.Open> {
            if (renderType != RenderType.CUSTOM_GUI) mc.resizeGui()
        }

        on<TerminalEvent.Close> {
            if (renderType != RenderType.CUSTOM_GUI) mc.resizeGui()
        }

        on<TickEvent.Server> {
            TerminalUtils.currentTerm?.ticksOpened++
        }
    }

    fun GuiGraphicsExtractor.renderDebug() {
        if (debug) TerminalUtils.currentTerm?.let { term ->
            val menu = (mc.gui.screen() as? AbstractContainerScreen<*>)?.menu ?: return@let
            val debugInfo = listOf(
                "§7Type: §f${term.type.name}",
                "§7Window Name: §f${mc.gui.screen()?.title?.string}",
                "§7Container ID: §f${menu.containerId}",
                "§7Time Open: §f${System.currentTimeMillis() - term.timeOpened}ms",
                "§7Ticks Open: §f${term.ticksOpened}",
                "§7Solution: §f${term.solution.joinToString(", ")}",
                "§7Clicked Slots: §f${term.clickedSlots}",
            )

            pose().pushMatrix()
            val sf = mc.window.guiScale
            pose().scale(1f / sf, 1f / sf)
            pose().scale(3f)

            val items = menu.items.subList(0, term.type.windowSize)
            val blackPane = Items.STAINED_GLASS_PANE.pick(DyeColor.BLACK)
            textWithWordWrap(mc.font, Component.literal(items.filter { !it.isEmpty && it.item != blackPane }.map { stack -> stack.hoverName.string }.toString()), 400, 0, 300, Colors.WHITE.rgba)

            debugInfo.forEachIndexed { index, line ->
                textWithWordWrap(mc.font, Component.literal(line), 5, 20 + (index * 10), 300, Colors.WHITE.rgba)
            }

            items.forEachIndexed { index, stack ->
                item(stack, 5 + (index % 9) * 18, 250 + (index / 9) * 18)
                itemDecorations(mc.font, stack, 5 + (index % 9) * 18, 250 + (index / 9) * 18)
            }
            pose().popMatrix()
        }
    }

    enum class RenderType(private val label: String) {
        ODIN("Odin"),
        NORMAL("Normal"),
        CUSTOM_GUI("Custom GUI");

        override fun toString(): String = label
    }

    enum class RubixMode { FEWEST_CLICKS, LEFT_CLICKS_ONLY }
}

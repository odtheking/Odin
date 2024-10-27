package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.TerminalClosedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalSolver.currentTerm
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.impl.floor7.p3.termGUI.TermGui
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.util.MouseUtils.mouseX
import me.odinmain.ui.util.MouseUtils.mouseY
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HoverTerms : Module(
    name = "Hover Terms",
    description = "Clicks the hovered item in a terminal if it is correct.",
    category = Category.FLOOR7,
    tag = TagType.RISKY
) {
    private val triggerDelay by NumberSetting("Delay", 200L, 50, 800, unit = "ms", description = "Delay between clicks.")
    private val firstClickDelay by NumberSetting("First Click Delay", 200L, 50, 500, unit = "ms", description = "Delay before first click.")
    private val middleClick by DualSetting("Click Type", "Left", "Middle", default = true, description = "What Click type to use.")
    private val previouslyClicked = mutableSetOf<Int>()
    private val triggerBotClock = Clock(triggerDelay)
    private var clickedThisWindow = false

    @SubscribeEvent(receiveCanceled = true)
    fun onDrawGuiContainer(event: GuiEvent.DrawGuiContainerScreenEvent) {
        if (
            TerminalSolver.currentTerm.type == TerminalTypes.NONE ||
            TerminalSolver.currentTerm.solution.isEmpty() ||
            !triggerBotClock.hasTimePassed(triggerDelay) ||
            System.currentTimeMillis() - currentTerm.timeOpened <= firstClickDelay ||
            clickedThisWindow
        ) return

        val hoveredItem =
            when {
                TerminalSolver.renderType == 3 && TerminalSolver.enabled -> TermGui.getHoveredItem(mouseX.toInt(), mouseY.toInt())
                else -> {
                    if (event.gui.slotUnderMouse?.inventory == mc.thePlayer?.inventory) return
                    event.gui.slotUnderMouse?.slotIndex
                }
            } ?: return

        if (hoveredItem !in TerminalSolver.currentTerm.solution || hoveredItem in previouslyClicked) return

        when (currentTerm.type) {
            TerminalTypes.RUBIX -> {
                clickedThisWindow = true
                windowClick(hoveredItem, if (TerminalSolver.currentTerm.solution.count { it == hoveredItem } >= 3) PlayerUtils.ClickType.Right else if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left)
                triggerBotClock.update()
                if (TerminalSolver.currentTerm.solution.count { it == hoveredItem } < 1) previouslyClicked += hoveredItem
            }

            TerminalTypes.ORDER -> {
                if (TerminalSolver.currentTerm.solution.first() == hoveredItem) {
                    clickedThisWindow = true
                    windowClick(hoveredItem, if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left)
                    triggerBotClock.update()
                    previouslyClicked += hoveredItem
                }
            }

            TerminalTypes.MELODY ->
                if (hoveredItem % 9 == 7) {
                    clickedThisWindow = true
                    windowClick(hoveredItem, if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left)
                    triggerBotClock.update()
                    previouslyClicked += hoveredItem
                }

            TerminalTypes.PANES, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT -> {
                clickedThisWindow = true
                windowClick(hoveredItem, if (middleClick) PlayerUtils.ClickType.Middle else PlayerUtils.ClickType.Left)
                triggerBotClock.update()
                previouslyClicked += hoveredItem
            }
            else -> return
        }
    }

    @SubscribeEvent
    fun onTerminalLeft(event: TerminalClosedEvent) {
        clickedThisWindow = false
        previouslyClicked.clear()
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiEvent.GuiLoadedEvent) {
        clickedThisWindow = false
    }
}
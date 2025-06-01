package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.impl.floor7.p3.termGUI.TermGui
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.ui.util.MouseUtils.mouseX
import me.odinmain.utils.ui.util.MouseUtils.mouseY
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HoverTerms : Module(
    name = "Hover Terms",
    desc = "Clicks the hovered item in a terminal if it is correct.",
    tag = TagType.RISKY
) {
    private val triggerDelay by NumberSetting("Delay", 170L, 130, 300, unit = "ms", desc = "Delay between clicks.")
    private val firstClickDelay by NumberSetting("First Click Delay", 350L, 300, 500, unit = "ms", desc = "Delay before first click.")
    private val triggerBotClock = Clock(triggerDelay)

    @SubscribeEvent(receiveCanceled = true)
    fun onDrawGuiContainer(event: GuiEvent.DrawGuiBackground) = with (TerminalSolver.currentTerm) {
        if (
            this?.type == null ||
            solution.isEmpty() ||
            !triggerBotClock.hasTimePassed(triggerDelay) ||
            System.currentTimeMillis() - timeOpened <= firstClickDelay ||
            isClicked
        ) return

        val hoveredItem =
            when {
                TerminalSolver.renderType == 3 && TerminalSolver.enabled -> TermGui.getHoveredItem(mouseX.toInt(), mouseY.toInt())
                else -> {
                    if (event.gui.slotUnderMouse?.inventory == mc.thePlayer?.inventory) return
                    event.gui.slotUnderMouse?.slotIndex
                }
            } ?: return

        when (type) {
            TerminalTypes.RUBIX -> {
                val needed = solution.count { it == hoveredItem } >= 3
                if (!canClick(hoveredItem, if (needed) 1 else 0)) return
                click(hoveredItem, if (needed) ClickType.Right else ClickType.Middle)
                triggerBotClock.update()
            }

            TerminalTypes.NUMBERS ->
                if (canClick(hoveredItem, 2)) {
                    click(hoveredItem, ClickType.Middle)
                    triggerBotClock.update()
                }

            TerminalTypes.MELODY ->
                if (canClick(hoveredItem, 0)) {
                    click(hoveredItem, ClickType.Left)
                    triggerBotClock.update()
                }

            TerminalTypes.PANES, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT -> {
                if (canClick(hoveredItem, 2)) {
                    click(hoveredItem, ClickType.Middle)
                    triggerBotClock.update()
                }
            }
            else -> return
        }
    }
}
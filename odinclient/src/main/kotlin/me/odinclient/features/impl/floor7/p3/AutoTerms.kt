package me.odinclient.features.impl.floor7.p3

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.ClickType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoTerms : Module(
    name = "Auto Terms",
    description = "Automatically solves terminals."
) {
    private val autoDelay by NumberSetting("Delay", 170L, 100, 300, unit = "ms", desc = "Delay between clicks.")
    private val firstClickDelay by NumberSetting("First Click Delay", 350L, 300, 500, unit = "ms", desc = "Delay before first click.")
    private val breakThreshold by NumberSetting("Break Threshold", 500L, 350L, 1000L, 10L, unit = "ms", desc = "Time before breaking the click.")
    private val disableMelody by BooleanSetting("Disable Melody", false, desc = "Disables melody terminals.")
    private var lastClickTime = 0L
    private var firstClick = true

    @SubscribeEvent(receiveCanceled = true)
    fun onDrawGuiContainer(event: GuiEvent.DrawGuiBackground) = with (TerminalSolver.currentTerm) {
        if (this?.type == null) return

        if (firstClick && (System.currentTimeMillis() - lastClickTime < firstClickDelay)) return

        if (System.currentTimeMillis() - lastClickTime < autoDelay) return

        if (System.currentTimeMillis() - lastClickTime > breakThreshold) isClicked = false

        if (solution.isEmpty() || (disableMelody && type == TerminalTypes.MELODY) || isClicked) return

        val item = solution.firstOrNull() ?: return

        lastClickTime = System.currentTimeMillis()
        firstClick = false

        when (type) {
            TerminalTypes.RUBIX ->
                click(item, if (solution.count { it == item } >= 3) ClickType.Right else ClickType.Middle, false)

            TerminalTypes.MELODY ->
                click(solution.find { it % 9 == 7 } ?: return, ClickType.Middle, false)

            else -> click(item, ClickType.Middle, false)
        }
    }

    @SubscribeEvent
    fun onTerminalOpen(event: TerminalEvent.Opened) {
        lastClickTime = System.currentTimeMillis()
        firstClick = true
    }
}

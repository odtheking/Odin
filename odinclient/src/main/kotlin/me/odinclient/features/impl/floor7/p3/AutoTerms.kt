package me.odinclient.features.impl.floor7.p3

import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.ClickType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoTerms : Module(
    name = "Auto Terms",
    desc = "Automatically solves terminals.",
    tag = TagType.RISKY
) {
    private val autoDelay by NumberSetting("Delay", 170L, 130, 300, unit = "ms", desc = "Delay between clicks.")
    private val firstClickDelay by NumberSetting("First Click Delay", 350L, 300, 500, unit = "ms", desc = "Delay before first click.")
    private val breakThreshold by NumberSetting("Break Threshold", 500L, 350L, 1000L, 10L, unit = "ms", desc = "Time before breaking the click.")
    private val disableMelody by BooleanSetting("Disable Melody", false, desc = "Disables melody terminals.")
    private var lastClickTime = 0L
    private var firstClick = true

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) = with(TerminalSolver.currentTerm) {
        if (event.phase != TickEvent.Phase.START) return
        if (this?.type == null) {
            lastClickTime = System.currentTimeMillis()
            firstClick = true
            return
        }

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
}

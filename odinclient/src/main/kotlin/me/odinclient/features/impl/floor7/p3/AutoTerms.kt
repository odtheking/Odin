package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoTerms : Module(
    name = "Auto Terms",
    description = "Automatically solves terminals.",
    category = Category.FLOOR7,
    tag = TagType.RISKY
) {
    private val autoDelay by NumberSetting("Delay", 170L, 130, 300, unit = "ms", description = "Delay between clicks.")
    private val firstClickDelay by NumberSetting("First Click Delay", 350L, 300, 500, unit = "ms", description = "Delay before first click.")
    private val middleClick by BooleanSetting("Middle Click", false, description = "Use middle click instead of left click.")
    private val breakThreshold by NumberSetting("Break Threshold", 500L, 350L, 1000L, 10L, unit = "ms", description = "Time before breaking the click.")
    private val clickingOrder by SelectorSetting("Clicking order", "random", arrayListOf("from first", "from last", "random"), description = "The order to click the items in.")
    private val disableMelody by BooleanSetting("Disable Melody", false, description = "Disables melody terminals.")
    private val timeBetweenClicks by BooleanSetting("Time Between Clicks", false, description = "Prints the time between clicks.")
    private var clickedThisWindow = false
    private var lastClickTime = 0L
    private var firstClick = true

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (TerminalSolver.currentTerm.type == TerminalTypes.NONE) {
            lastClickTime = System.currentTimeMillis()
            firstClick = true
            return
        }

        if (firstClick && System.currentTimeMillis() - lastClickTime < firstClickDelay) return

        if (System.currentTimeMillis() - lastClickTime < autoDelay) return

        if (System.currentTimeMillis() - lastClickTime < breakThreshold) clickedThisWindow = false

        if (TerminalSolver.currentTerm.solution.isEmpty() || TerminalSolver.currentTerm.type == TerminalTypes.NONE ||
            (disableMelody && TerminalSolver.currentTerm.type == TerminalTypes.MELODY) || clickedThisWindow) return

        val item =
            if (clickingOrder == 0) TerminalSolver.currentTerm.solution.firstOrNull() ?: return else if (clickingOrder == 1) TerminalSolver.currentTerm.solution.lastOrNull() ?: return else TerminalSolver.currentTerm.solution.random()

        when (TerminalSolver.currentTerm.type) {
            TerminalTypes.RUBIX ->
                windowClick(item, if (TerminalSolver.currentTerm.solution.count { it == item } >= 3) ClickType.Right else if (middleClick) ClickType.Middle else ClickType.Left)

            TerminalTypes.ORDER ->
                windowClick(TerminalSolver.currentTerm.solution.first(), if (middleClick) ClickType.Middle else ClickType.Left)

            TerminalTypes.MELODY ->
                windowClick(TerminalSolver.currentTerm.solution.find { it % 9 == 7 } ?: return, if (middleClick) ClickType.Middle else ClickType.Left)

            else -> windowClick(item, if (middleClick) ClickType.Middle else ClickType.Left)
        }

        val currentTime = System.currentTimeMillis()
        if (timeBetweenClicks) modMessage("Time between clicks: ${currentTime - lastClickTime}ms")

        lastClickTime = currentTime
        clickedThisWindow = true
        firstClick = false
    }

    @SubscribeEvent
    fun onGuiLoaded(event: GuiEvent.Loaded) {
        clickedThisWindow = false
    }
}

package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.utils.skyblock.devMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object QueueTerms : Module(
    name = "Queue Terms",
    category = Category.FLOOR7,
    description = "Queues clicks in terminals to ensure every click is registered (only works in custom term gui).",
    tag = TagType.RISKY
) {
    private val dispatchDelay by NumberSetting("Dispatch Delay", 140L, 140L, 300L, unit = "ms", description = "The delay between each click.")
    private data class Click(val slot: Int, val mode: Int, val button: Int)
    private val previouslyClicked = mutableSetOf<Int>()
    private val queue = mutableListOf<Click>()
    private var clickedThisWindow = false
    private var lastClickTime = 0L

    @SubscribeEvent
    fun onGuiOpen(event: GuiEvent.Loaded) {
        clickedThisWindow = false
    }

    @SubscribeEvent
    fun onTerminalLeft(event: TerminalEvent.Closed) {
        clickedThisWindow = false
        previouslyClicked.clear()
        queue.clear()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (TerminalSolver.currentTerm.type.equalsOneOf(TerminalTypes.NONE, TerminalTypes.MELODY) || TerminalSolver.renderType != 3) return
        if (
            event.phase != TickEvent.Phase.START ||
            System.currentTimeMillis() - lastClickTime < dispatchDelay ||
            queue.isEmpty() ||
            clickedThisWindow
        ) return
        val click = queue.removeFirst()
        clickedThisWindow = true
        windowClick(slotId = click.slot, if (click.mode == 0) ClickType.Middle else ClickType.Right)
        lastClickTime = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onCustomTermClick(event: GuiEvent.CustomTermGuiClick) {
        if (TerminalSolver.currentTerm.type == TerminalTypes.NONE || TerminalSolver.renderType != 3) return
        event.isCanceled = true
        handleWindowClick(event.slot, event.mode, event.button)
    }

    private fun handleWindowClick(slot: Int, mode: Int, button: Int) {
        if ((TerminalSolver.currentTerm.type == TerminalTypes.ORDER && slot != TerminalSolver.currentTerm.solution.first()) || TerminalSolver.renderType != 3) return
        if (slot in previouslyClicked) return
        if (TerminalSolver.currentTerm.type == TerminalTypes.RUBIX) {
            if (TerminalSolver.currentTerm.solution.count { it == slot }.equalsOneOf(1, 4)) previouslyClicked += slot
        } else previouslyClicked += slot
        queue.takeIf { it.count { click -> click.slot == slot } < 2 }?.add(Click(slot = slot, mode = mode, button = button))
        devMessage("Queued click on slot $slot")
    }
}
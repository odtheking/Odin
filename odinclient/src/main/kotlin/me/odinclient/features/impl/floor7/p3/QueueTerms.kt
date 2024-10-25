package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.TerminalClosedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object QueueTerms : Module(
    name = "Queue Terms",
    category = Category.FLOOR7,
    description = "Queues clicks in terminals to ensure every click is registered."
) {
    private data class Click(val slot: Int, val mode: Int, val button: Int)
    private var clickedThisWindow = false
    private val queue = mutableListOf<Click>()
    private var lastClickTime = 0L

    @SubscribeEvent
    fun onGuiOpen(event: GuiEvent.GuiLoadedEvent) {
        clickedThisWindow = false
    }

    @SubscribeEvent
    fun onTerminalLeft(event: TerminalClosedEvent) {
        clickedThisWindow = false
        queue.clear()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        TerminalSolver.currentTerm.solution = TerminalSolver.currentTerm.solution.filter { it !in queue.map { it.slot } }
        if (
            event.phase != TickEvent.Phase.START ||
            System.currentTimeMillis() - lastClickTime < 140 ||
            TerminalSolver.currentTerm.type == TerminalTypes.NONE ||
            queue.isEmpty() ||
            clickedThisWindow
        ) return
        val click = queue.removeFirst()
        clickedThisWindow = true

        windowClick(slotId = click.slot, button = click.button, mode = click.mode)
        lastClickTime = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onCustomTermClick(event: GuiEvent.CustomTermGuiClick) {
        if (TerminalSolver.currentTerm.type == TerminalTypes.NONE) return
        event.isCanceled = true
        handleWindowClick(event.slot, event.mode, event.button)
    }

    fun handleWindowClick(slot: Int, mode: Int, button: Int) {
        if (slot !in TerminalSolver.currentTerm.solution) return
        if (TerminalSolver.currentTerm.type == TerminalTypes.ORDER && slot != TerminalSolver.currentTerm.solution.first()) return
        queue.add(Click(slot = slot, mode = mode, button = button))
    }
}
package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.TerminalClosedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.utils.skyblock.devMessage
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.fml.common.eventhandler.EventPriority
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
        if (
            event.phase != TickEvent.Phase.START ||
            System.currentTimeMillis() - lastClickTime < 140 ||
            TerminalSolver.currentTerm.type == TerminalTypes.NONE ||
            queue.isEmpty() ||
            clickedThisWindow
        ) return
        val click = queue.removeFirst()
        windowClick(slotId = click.slot, button = click.button, mode = click.mode)
        lastClickTime = System.currentTimeMillis()
        clickedThisWindow = true
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onMouseClick(event: GuiEvent.GuiMouseClickEvent) {
        if (TerminalSolver.currentTerm.type == TerminalTypes.NONE || event.isCanceled) return
        if (!clickedThisWindow) {
            clickedThisWindow = true
            return
        }
        val slot = (event.gui as? GuiContainer)?.slotUnderMouse?.slotIndex ?: return
        event.isCanceled = true
        handleWindowClick(slot, 0, event.button)
    }

    @SubscribeEvent
    fun onCustomTermClick(event: GuiEvent.CustomTermGuiClick) {
        if (TerminalSolver.currentTerm.type == TerminalTypes.NONE) return
        if (!clickedThisWindow) {
            clickedThisWindow = true
            return
        }
        event.isCanceled = true
        handleWindowClick(event.slot, event.mode, event.button)
    }

    fun handleWindowClick(slot: Int, mode: Int, button: Int) {
        if (slot !in TerminalSolver.currentTerm.solution) return
        if (TerminalSolver.currentTerm.type == TerminalTypes.ORDER && slot != TerminalSolver.currentTerm.solution.first()) return
        clickedThisWindow = true
        queue.add(Click(slot = slot, mode = mode, button = button))
        devMessage("added ${queue.last()}")
    }
}
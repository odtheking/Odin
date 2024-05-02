package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.TerminalClosedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.Event
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


    @SubscribeEvent
    fun onGuiOpen(event: GuiEvent.GuiLoadedEvent) {
        if (queue.isNotEmpty()) {
            val container = mc.thePlayer?.openContainer ?: return
            val click = queue.firstOrNull() ?: return
            PlayerUtils.windowClick(slotId = click.slot, button = click.button, mode = click.mode)
            queue.removeFirst()
            clickedThisWindow = true
        } else clickedThisWindow = false
    }

    @SubscribeEvent
    fun onTerminalLeft(event: TerminalClosedEvent) {
        clickedThisWindow = false
        queue.clear()
    }

    @SubscribeEvent
    fun onMouseClick(event: GuiEvent.GuiMouseClickEvent) {
        if (TerminalSolver.currentTerm == TerminalTypes.NONE ) return
        if (!clickedThisWindow) {
            clickedThisWindow = true
            return
        }

        event.isCanceled = true
        clickedThisWindow = true
        queue.add(Click(slot = (event.gui as? GuiContainer)?.slotUnderMouse?.slotIndex ?: return, mode = if (event.button == 2) 3 else 0, button = event.button))
        modMessage("added ${queue.last()}")
    }
}
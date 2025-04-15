package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.devMessage
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

object QueueTerms : Module(
    name = "Queue Terms",
    desc = "Queues clicks in terminals to ensure every click is registered (only works in custom term gui).",
    tag = TagType.RISKY
) {
    private val dispatchDelay by NumberSetting("Dispatch Delay", 140L, 140L, 300L, unit = "ms", desc = "The delay between each click.")
    private data class Click(val slot: Int, val button: Int)
    private val queue: Queue<Click> = LinkedList()
    private var lastClickTime = 0L

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onCustomTermClick(event: GuiEvent.CustomTermGuiClick) {
        with (TerminalSolver.currentTerm ?: return) {
            if (type == TerminalTypes.MELODY || TerminalSolver.renderType != 3 || !isClicked || !canClick(event.slot, event.button)) return

            queue.offer(Click(event.slot, event.button))
            simulateClick(event.slot, if (event.button == 0) ClickType.Middle else ClickType.Right)

            devMessage("§aQueued click on slot ${event.slot}")
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        with (TerminalSolver.currentTerm ?: return) {
            if (
                event.phase != TickEvent.Phase.START ||
                type == TerminalTypes.MELODY ||
                TerminalSolver.renderType != 3 ||
                System.currentTimeMillis() - lastClickTime < dispatchDelay ||
                queue.isEmpty() ||
                isClicked
            ) return
            val click = queue.poll() ?: return
            click(click.slot, if (click.button == 0) ClickType.Middle else ClickType.Right, false)
            devMessage("§dDispatched click on slot ${click.slot}")
            lastClickTime = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onTerminalUpdate(event: TerminalEvent.Updated) {
        with (TerminalSolver.currentTerm ?: return) {
            if (type == TerminalTypes.MELODY || TerminalSolver.renderType != 3 || queue.isEmpty()) return
            if (queue.all { it.slot in solution }) queue.forEach { simulateClick(it.slot, if (it.button == 0) ClickType.Middle else ClickType.Right) }
        }
    }

    @SubscribeEvent
    fun onTerminalLeft(event: TerminalEvent.Closed) {
        queue.clear()
    }
}
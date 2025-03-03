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
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object QueueTerms : Module(
    name = "Queue Terms",
    category = Category.FLOOR7,
    description = "Queues clicks in terminals to ensure every click is registered (only works in custom term gui).",
    tag = TagType.RISKY
) {
    private val dispatchDelay by NumberSetting("Dispatch Delay", 140L, 140L, 300L, unit = "ms", description = "The delay between each click.")
    private data class Click(val slot: Int, val button: Int)
    private val queue = mutableListOf<Click>()
    private var clickedThisWindow = false
    private var lastClickTime = 0L

    init {
        onPacket<S2DPacketOpenWindow> {
            clickedThisWindow = false
        }
    }

    @SubscribeEvent
    fun onTerminalLeft(event: TerminalEvent.Closed) {
        clickedThisWindow = false
        queue.clear()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (
            TerminalSolver.currentTerm.type.equalsOneOf(TerminalTypes.NONE, TerminalTypes.MELODY) ||
            TerminalSolver.renderType != 3 ||
            event.phase != TickEvent.Phase.START ||
            System.currentTimeMillis() - lastClickTime < dispatchDelay ||
            queue.isEmpty() ||
            clickedThisWindow
        ) return
        val click = queue.removeFirst().takeIf { TerminalSolver.canClick(it.slot, it.button) } ?: return
        clickedThisWindow = true
        windowClick(slotId = click.slot, if (click.button == 1) ClickType.Right else ClickType.Middle)
        devMessage("Dispatched click on slot ${click.slot}")
        lastClickTime = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onCustomTermClick(event: GuiEvent.CustomTermGuiClick) {
        if (TerminalSolver.currentTerm.type.equalsOneOf(TerminalTypes.NONE, TerminalTypes.MELODY) || TerminalSolver.renderType != 3) return
        queue.takeIf { it.count { click -> click.slot == event.slot } < 2 }?.add(Click(slot = event.slot, button = event.button))
        devMessage("Queued click on slot ${event.slot}")
        event.isCanceled = true
    }
}
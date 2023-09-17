package me.odinclient.features.impl.floor7.p3

import me.odinclient.events.impl.PacketSentEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CancelWrongTerms : Module(
    name = "Stop Wrong Clicks",
    description = "Stops you from clicking wrong items in terminals",
    category = Category.FLOOR7
) {
    @SubscribeEvent
    fun onSlotClick(event: PacketSentEvent) {
        if (event.packet !is C0EPacketClickWindow || TerminalSolver.currentTerm == -1) return
        if (event.packet.slotId in TerminalSolver.solution) return
        event.isCanceled = true
    }
}
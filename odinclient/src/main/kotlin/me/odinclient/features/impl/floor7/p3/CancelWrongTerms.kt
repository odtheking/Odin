package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CancelWrongTerms : Module(
    name = "Stop Wrong Clicks",
    description = "Stops you from clicking wrong items in terminals.",
    category = Category.FLOOR7
) {
    @SubscribeEvent
    fun onSlotClick(event: PacketSentEvent) {
        if (event.packet !is C0EPacketClickWindow || TerminalSolver.currentTerm == TerminalTypes.NONE) return
        if ((event.packet as C0EPacketClickWindow).slotId in TerminalSolver.solution) return
        event.isCanceled = true
    }
}
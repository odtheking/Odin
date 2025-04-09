package me.odinmain.features.impl.floor7.p3.terminalhandler

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot

class PanesHandler: TerminalHandler(TerminalTypes.PANES) {

    override fun handleSlotUpdate(packet: S2FPacketSetSlot): Boolean {
        if (packet.func_149173_d() != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solvePanes(items))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: ClickType) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    private fun solvePanes(items: Array<ItemStack?>): List<Int> =
        items.mapIndexedNotNull { index, item -> if (item?.metadata == 14) index else null }
}
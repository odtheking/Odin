package me.odinmain.features.impl.floor7.p3.terminalhandler

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot

class NumbersHandler: TerminalHandler(TerminalTypes.NUMBERS) {

    override fun handleSlotUpdate(packet: S2FPacketSetSlot): Boolean {
        if (packet.func_149173_d() != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveNumbers(items))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: ClickType) {
        if (solution.indexOf(slotIndex) == 0) solution.removeAt(0)
    }

    private fun solveNumbers(items: Array<ItemStack?>): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item?.metadata == 14 && Item.getIdFromItem(item.item) == 160) index else null
        }.sortedBy { items[it]?.stackSize }
    }
}
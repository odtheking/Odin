package me.odinmain.features.impl.floor7.p3.terminalhandler

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot

class StartsWithHandler(private val letter: String): TerminalHandler(TerminalTypes.STARTS_WITH) {

    override fun handleSlotUpdate(packet: S2FPacketSetSlot): Boolean {
        if (packet.func_149173_d() != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveStartsWith(items, letter))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: ClickType) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    private fun solveStartsWith(items: Array<ItemStack?>, letter: String): List<Int> =
        items.mapIndexedNotNull { index, item -> if (item?.unformattedName?.startsWith(letter, true) == true && !item.isItemEnchanted) index else null }
}
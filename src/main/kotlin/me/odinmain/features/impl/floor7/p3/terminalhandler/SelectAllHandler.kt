package me.odinmain.features.impl.floor7.p3.terminalhandler

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot

class SelectAllHandler(private val colorName: String): TerminalHandler(TerminalTypes.SELECT) {

    override fun handleSlotUpdate(packet: S2FPacketSetSlot): Boolean {
        if (packet.func_149173_d() != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveSelectAll(items, colorName))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: ClickType) {
        solution.removeAt(solution.indexOf(slotIndex).takeIf { it != -1 } ?: return)
    }

    private fun solveSelectAll(items: Array<ItemStack?>, color: String): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item?.isItemEnchanted == false &&
                Item.getIdFromItem(item.item) != 160 &&
                item.unlocalizedName?.contains(color, true) == true &&
                (color == "lightBlue" || item.unlocalizedName?.contains("lightBlue", true) == false)
            ) index else null
        }
    }
}
package me.odinmain.features.impl.floor7.p3.terminalhandler

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot

class MelodyHandler: TerminalHandler(TerminalTypes.MELODY) {

    override fun handleSlotUpdate(packet: S2FPacketSetSlot): Boolean {
        return packet.func_149174_e()?.let {
            solution.clear()
            solution.addAll(solveMelody(items))
        } != null
    }

    private fun solveMelody(items: Array<ItemStack?>): List<Int> {
        val greenPane = items.indexOfLast { it?.metadata == 5 && Item.getIdFromItem(it.item) == 160 }.takeIf { it != -1 } ?: return emptyList()
        val magentaPane = items.indexOfFirst { it?.metadata == 2 && Item.getIdFromItem(it.item) == 160 }.takeIf { it != -1 } ?: return emptyList()
        val greenClay = items.indexOfLast { it?.metadata == 5 && Item.getIdFromItem(it.item) == 159 }.takeIf { it != -1 } ?: return emptyList()
        return items.mapIndexedNotNull { index, item ->
            when {
                index == greenPane || item?.metadata == 2 && Item.getIdFromItem(item.item) == 160 -> index
                index == greenClay && greenPane % 9 == magentaPane % 9 -> index
                else -> null
            }
        }
    }
}
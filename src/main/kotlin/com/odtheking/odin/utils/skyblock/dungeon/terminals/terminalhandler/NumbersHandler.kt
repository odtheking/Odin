package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class NumbersHandler: TerminalHandler(TerminalTypes.NUMBERS) {

    override fun solve(items: List<ItemStack>): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item.item == Items.RED_STAINED_GLASS_PANE) index else null
        }.sortedBy { items[it].count }
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        solution.removeAt(0)
    }

    override fun canClick(slotIndex: Int, button: Int): Boolean = slotIndex == solution.firstOrNull()
}
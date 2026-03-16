package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MelodyHandler: TerminalHandler(TerminalTypes.MELODY) {

    override fun canSolve(items: List<ItemStack>, currentIndex: Int): Boolean = true

    override fun solve(items: List<ItemStack>): List<Int> {
        val magentaPane = items.indexOfFirst { it.item == Items.MAGENTA_STAINED_GLASS_PANE }
        val greenPane = items.indexOfLast { it.item == Items.LIME_STAINED_GLASS_PANE }
        val greenClay = items.indexOfLast { it.item == Items.LIME_TERRACOTTA }

        return items.mapIndexedNotNull { index, item ->
            when {
                index == greenPane || item.item == Items.MAGENTA_STAINED_GLASS_PANE -> index
                index == greenClay && greenPane % 9 == magentaPane % 9 -> index
                else -> null
            }
        }
    }

    override fun canClick(slotIndex: Int, button: Int): Boolean =
        slotIndex.equalsOneOf(16, 25, 34, 43)
}
package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class PanesHandler: TerminalHandler(TerminalTypes.PANES) {

    override fun solve(items: List<ItemStack>): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item.item == Items.RED_STAINED_GLASS_PANE) index else null
        }
    }
}
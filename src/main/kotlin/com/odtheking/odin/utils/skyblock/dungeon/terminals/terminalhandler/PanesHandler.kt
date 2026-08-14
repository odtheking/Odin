package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class PanesHandler: TerminalHandler(TerminalTypes.PANES) {

    override fun solve(items: List<ItemStack>): List<Int> {
        val redPane = Items.STAINED_GLASS_PANE.pick(DyeColor.RED)
        return items.mapIndexedNotNull { index, item ->
            if (item.item == redPane) index else null
        }
    }

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = TerminalSolver.panesColor to null
}
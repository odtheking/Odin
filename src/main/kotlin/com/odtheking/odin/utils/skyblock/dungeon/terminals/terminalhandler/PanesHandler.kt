package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Items

class PanesHandler: TerminalHandler(TerminalTypes.PANES) {

    override fun solve(slots: List<Slot>, updatedIndex: Int): List<Int> =
        slots.mapIndexedNotNull { index, slot ->
            if (slot.item.item == Items.RED_STAINED_GLASS_PANE) index else null
        }

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = TerminalSolver.panesColor to null
}
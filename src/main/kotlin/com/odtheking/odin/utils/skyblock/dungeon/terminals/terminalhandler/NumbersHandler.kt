package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Items
import kotlin.math.abs

class NumbersHandler: TerminalHandler(TerminalTypes.NUMBERS) {

    override fun solve(slots: List<Slot>, updatedIndex: Int): List<Int> {
        val redPane = Items.STAINED_GLASS_PANE.pick(DyeColor.RED)
        return slots.mapIndexedNotNull { index, slot ->
            if (slot.item.item == redPane) index else null
        }.sortedBy { slots[it].item.count }
    }

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        solution.removeAt(0)
    }

    override fun canClick(slotIndex: Int, button: Int): Boolean = slotIndex == solution.firstOrNull()

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> {
        val solutionIndex = solution.indexOf(slotIndex)
        return when (solutionIndex) {
            0 -> TerminalSolver.numbers1Color
            1 -> TerminalSolver.numbers2Color
            2 -> TerminalSolver.numbers3Color
            3 -> TerminalSolver.numbers4Color
            else -> Colors.TRANSPARENT
        } to (abs((solution.size - 14) - solutionIndex) + 1).toString()
    }
}
package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.abs

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

    override fun renderSlot(slotIndex: Int): Pair<Color, String?>? {
        val solutionIndex = solution.indexOf(slotIndex)
        return when (solutionIndex) {
            0 -> TerminalSolver.orderColor
            1 -> TerminalSolver.orderColor2
            2 -> TerminalSolver.orderColor3
            else -> return null
        } to if (TerminalSolver.showNumbers) (abs((solution.size - 14) - solutionIndex) + 1).toString() else null
    }
}
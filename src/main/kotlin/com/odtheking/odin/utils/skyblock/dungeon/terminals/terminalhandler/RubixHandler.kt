package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.StainedGlassPaneBlock

class RubixHandler : TerminalHandler(TerminalTypes.RUBIX) {

    private var lockedColor: DyeColor? = null

    override fun solve(slots: List<Slot>, updatedIndex: Int): List<Int> {
        val panes = slots.mapIndexedNotNull { index, slot ->
            slot.item.paneColor?.takeUnless { it == DyeColor.BLACK }?.let { index to it }
        }

        return if (updatedIndex == LAST_PANE_SLOT && lockedColor == null) {
            val (best, clicks) = rubixColorOrder
                .map { it to clicksFor(it, panes) }
                .minBy { (_, clicks) -> getRealSize(clicks) }

            lockedColor = best
            clicks
        } else lockedColor?.let { clicksFor(it, panes) } ?: emptyList()
    }

    private fun clicksFor(goal: DyeColor, panes: List<Pair<Int, DyeColor>>): List<Int> {
        val goalIndex = rubixColorOrder.indexOf(goal)
        return panes.flatMap { (slotIndex, color) ->
            List(dist(rubixColorOrder.indexOf(color), goalIndex)) { slotIndex }
        }
    }

    private val ItemStack.paneColor: DyeColor?
        get() = ((item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color

    override fun simulateClick(slotIndex: Int, clickType: Int) {
        if (slotIndex !in solution) return
        if (clickType == 1) solution.add(slotIndex)
        else solution.remove(slotIndex)
    }

    override fun canClick(slotIndex: Int, button: Int): Boolean {
        if (slotIndex !in solution) return false
        val needed = solution.count { it == slotIndex }
        return !((needed < 3 && button == 1) || (needed.equalsOneOf(3, 4) && button != 1))
    }

    private fun getRealSize(clicks: List<Int>): Int =
        clicks.groupingBy { it }.eachCount().values.sumOf { if (it >= 3) 5 - it else it }

    private fun dist(pane: Int, most: Int): Int =
        if (pane > most) (most + rubixColorOrder.size) - pane else most - pane

    override fun renderSlot(slotIndex: Int): Pair<Color, String?>? {
        val amount = solution.count { it == slotIndex }
        val clicksRequired = if (amount < 3) amount else amount - 5
        if (clicksRequired == 0) return null
        return when (clicksRequired) {
            1 -> TerminalSolver.rubixColor1
            2 -> TerminalSolver.rubixColor2
            -1 -> TerminalSolver.oppositeRubixColor1
            else -> TerminalSolver.oppositeRubixColor2
        } to clicksRequired.toString()
    }

    private companion object {
        const val LAST_PANE_SLOT = 32
        val rubixColorOrder = listOf(DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED)
    }
}
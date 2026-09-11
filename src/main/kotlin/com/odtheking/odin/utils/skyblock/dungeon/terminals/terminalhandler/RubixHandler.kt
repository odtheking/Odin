package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.StainedGlassPaneBlock
import kotlin.math.abs

class RubixHandler : TerminalHandler(TerminalTypes.RUBIX) {

    private var lockedColor: DyeColor? = null

    private val rightClickSlots = HashSet<Int>()

    override fun solve(slots: List<Slot>, updatedIndex: Int): List<Int> {
        val panes = slots.mapIndexedNotNull { index, slot ->
            slot.item.paneColor?.takeUnless { it == DyeColor.BLACK }?.let { index to it }
        }

        if (updatedIndex == LAST_PANE_SLOT && lockedColor == null)
            lockedColor = rubixColorOrder.minBy { goal -> clicksFor(goal, panes).values.sumOf { abs(it) } }

        val clicks = lockedColor?.let { clicksFor(it, panes) }.orEmpty()

        rightClickSlots.clear()
        clicks.forEach { (slotIndex, count) -> if (count < 0) rightClickSlots.add(slotIndex) }
        return clicks.flatMap { (slotIndex, count) -> List(abs(count)) { slotIndex } }
    }

    private fun clicksFor(goal: DyeColor, panes: List<Pair<Int, DyeColor>>): Map<Int, Int> {
        val goalIndex = rubixColorOrder.indexOf(goal)
        return panes.associate { (slotIndex, color) ->
            val forward = dist(rubixColorOrder.indexOf(color), goalIndex)
            slotIndex to if (forward > 2 && TerminalSolver.rubixMode != 1) forward - rubixColorOrder.size else forward
        }.filterValues { it != 0 }
    }

    private val ItemStack.paneColor: DyeColor?
        get() = ((item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color

    override fun canClick(slotIndex: Int, button: Int): Boolean =
        slotIndex in solution && (button == 1) == (slotIndex in rightClickSlots)

    private fun dist(pane: Int, most: Int): Int =
        if (pane > most) (most + rubixColorOrder.size) - pane else most - pane

    override fun renderSlot(slotIndex: Int): Pair<Color, String?>? {
        val remaining = solution.count { it == slotIndex }.takeIf { it > 0 } ?: return null
        val clicksRequired = if (slotIndex in rightClickSlots) -remaining else remaining
        return when (clicksRequired) {
            1 -> TerminalSolver.rubixColor1
            2 -> TerminalSolver.rubixColor2
            -1, 4 -> TerminalSolver.oppositeRubixColor1
            else -> TerminalSolver.oppositeRubixColor2
        } to clicksRequired.toString()
    }

    private companion object {
        const val LAST_PANE_SLOT = 32
        val rubixColorOrder = listOf(DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED)
    }
}
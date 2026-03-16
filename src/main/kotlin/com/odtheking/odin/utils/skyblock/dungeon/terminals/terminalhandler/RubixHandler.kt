package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.StainedGlassPaneBlock

class RubixHandler : TerminalHandler(TerminalTypes.RUBIX) {

    private val rubixColorOrder = listOf(DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED)
    private var lastRubixSolution: DyeColor? = null

    override fun solve(items: List<ItemStack>): List<Int> {
        val panes = items.mapNotNull { item ->
            if (((item.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color != DyeColor.BLACK) item else null
        }

        var temp: List<Int> = List(100) { i -> i }

        if (lastRubixSolution != null) {
            val lastIndex = rubixColorOrder.indexOf(lastRubixSolution)

            temp = panes.flatMap { pane ->
                val paneDye = ((pane.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color ?: return@flatMap emptyList()
                val paneIdx = rubixColorOrder.indexOf(paneDye)
                if (paneIdx != lastIndex) List(dist(paneIdx, lastIndex)) { pane } else emptyList()
            }.map { items.indexOf(it) }

        } else {
            for (color in rubixColorOrder) {
                val goalIndex = rubixColorOrder.indexOf(color)

                val temp2 = panes.flatMap { pane ->
                    val paneDye = ((pane.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color
                        ?: return@flatMap emptyList()
                    val paneIdx = rubixColorOrder.indexOf(paneDye)
                    if (paneIdx != goalIndex) List(dist(paneIdx, goalIndex)) { pane } else emptyList()
                }.map { items.indexOf(it) }

                if (getRealSize(temp2) < getRealSize(temp)) {
                    temp = temp2
                    lastRubixSolution = color
                }
            }
        }

        return temp
    }

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

    private fun getRealSize(list: List<Int>): Int {
        var size = 0
        list.distinct().forEach { pane ->
            val count = list.count { it == pane }
            size += if (count >= 3) 5 - count else count
        }
        return size
    }

    private fun dist(pane: Int, most: Int): Int =
        if (pane > most) (most + rubixColorOrder.size) - pane else most - pane
}
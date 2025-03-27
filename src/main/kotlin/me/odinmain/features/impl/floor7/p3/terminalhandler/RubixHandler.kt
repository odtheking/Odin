package me.odinmain.features.impl.floor7.p3.terminalhandler

import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot

class RubixHandler: TerminalHandler(TerminalTypes.RUBIX) {

    override fun solve(packet: S2FPacketSetSlot): Boolean {
        if (packet.func_149173_d() != type.windowSize - 1) return false
        solution.clear()
        solution.addAll(solveRubix(items))
        return true
    }

    override fun simulateClick(slotIndex: Int, clickType: ClickType) {
        if (solution.none { it == slotIndex }) return
        val needed = solution.count { it == slotIndex }
        if (ClickType.Left == clickType) {
            if (needed < 3) solution.remove(slotIndex)
            else solution.add(slotIndex)
        } else {
            if (needed < 3) solution.remove(slotIndex)
            else solution.add(slotIndex)
        }
    }

    private var lastRubixSolution: Int? = null

    private val rubixColorOrder = listOf(1, 4, 13, 11, 14)
    private fun solveRubix(items: Array<ItemStack?>): List<Int> {
        val panes = items.mapNotNull { item -> if (item?.metadata != 15 && Item.getIdFromItem(item?.item) == 160) item else null }
        var temp = List(100) { i -> i }
        if (lastRubixSolution != null) {
            temp = panes.flatMap { pane ->
                if (pane.metadata != lastRubixSolution) {
                    Array(dist(rubixColorOrder.indexOf(pane.metadata), rubixColorOrder.indexOf(lastRubixSolution))) { pane }.toList()
                } else emptyList()
            }.map { items.indexOf(it) }
        } else {
            for (color in rubixColorOrder) {
                val temp2 = panes.flatMap { pane ->
                    if (pane.metadata != color) {
                        Array(dist(rubixColorOrder.indexOf(pane.metadata), rubixColorOrder.indexOf(color))) { pane }.toList()
                    } else emptyList()
                }.map { items.indexOf(it) }
                if (getRealSize(temp2) < getRealSize(temp)) {
                    temp = temp2
                    lastRubixSolution = color
                }
            }
        }
        return temp
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
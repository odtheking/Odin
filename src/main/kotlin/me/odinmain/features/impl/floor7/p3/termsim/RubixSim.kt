package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.postAndCatch
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object RubixSim : TermSimGUI(
    TerminalTypes.RUBIX.windowName, TerminalTypes.RUBIX.windowSize
) {
    private val indices = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)
    private val order = listOf(1, 4, 13, 11, 14)

    override fun create() {
        createNewGui {
            if (floor(it.slotIndex / 9f) in 1f..3f && it.slotIndex % 9 in 3..5) getPane()
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        val current = order.find { it == slot.stack?.metadata } ?: return
        createNewGui {
            if (it == slot) {
                if (button == 1) genStack(order.getOrElse(order.indexOf(current) - 1) { order.last() })
                else genStack(order[(order.indexOf(current) + 1) % order.size])
            } else it.stack ?: blackPane
        }

        playTermSimSound()
        if (indices.all { guiInventorySlots[it]?.stack?.metadata == guiInventorySlots[12]?.stack?.metadata })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun getPane(): ItemStack {
        return when (Math.random()) {
            in 0.0..0.2 -> genStack(order[0])
            in 0.2..0.4 -> genStack(order[1])
            in 0.4..0.6 -> genStack(order[2])
            in 0.6..0.8 -> genStack(order[3])
            else -> genStack(order[4])
        }
    }

    private fun genStack(meta: Int) = ItemStack(pane, 1, meta).apply { setStackDisplayName("") } // This makes unique itemstacks, so terminalsolver works.
}
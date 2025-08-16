package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.postAndCatch
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object NumbersSim : TermSimGUI(
    TerminalTypes.NUMBERS.windowName, TerminalTypes.NUMBERS.windowSize
) {
    override fun create() {
        val used = (1..14).shuffled().toMutableList()
        createNewGui {
            if (floor(it.slotIndex / 9f) in 1f..2f && it.slotIndex % 9 in 1..7) ItemStack(pane, used.removeFirst(), 14).apply { setStackDisplayName("") }
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (guiInventorySlots.minByOrNull { if (it.stack?.metadata == 14) it.stack?.stackSize ?: 999 else 1000 } != slot) return
        createNewGui {
            if (it == slot) ItemStack(pane, slot.stack.stackSize, 5).apply { setStackDisplayName("") }
            else it.stack ?: blackPane
        }
        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.metadata == 14 })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}
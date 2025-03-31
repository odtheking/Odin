package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.postAndCatch
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object PanesSim : TermSimGUI(
    TerminalTypes.PANES.windowName, TerminalTypes.PANES.windowSize
) {
    private val greenPane get() = ItemStack(pane, 1, 5 ).apply { setStackDisplayName("") }
    private val redPane   get() = ItemStack(pane, 1, 14).apply { setStackDisplayName("") }

    override fun create() {
        createNewGui {
            if (floor(it.slotIndex / 9.0) in 1.0..3.0 && it.slotIndex % 9 in 2..6) if (Math.random() > 0.75) greenPane else redPane else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        createNewGui { if (it == slot) { if (slot.stack?.metadata == 14) greenPane else redPane } else it.stack }

        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.metadata == 14 })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}
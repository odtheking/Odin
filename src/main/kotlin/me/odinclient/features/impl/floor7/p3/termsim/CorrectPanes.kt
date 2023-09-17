package me.odinclient.features.impl.floor7.p3.termsim

import me.odinclient.events.impl.GuiLoadedEvent
import me.odinclient.features.impl.floor7.p3.TerminalSolver
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object CorrectPanes : TermSimGui(
    "Correct all the panes!", 45
) {
    private val greenPane get() = ItemStack(pane, 1, 5 ).apply { setStackDisplayName("") }
    private val redPane   get() = ItemStack(pane, 1, 14).apply { setStackDisplayName("") }

    override fun create() {
        this.inventorySlots.inventorySlots.subList(0, 45).forEachIndexed { index, it ->
            if (floor(index / 9.0) in 1.0..3.0 && index % 9 in 2..6) it.putStack(getPane())
            else it.putStack(blackPane)
        }
    }

    private fun getPane(): ItemStack {
        val a = Math.random()
        return if (a > 0.75) greenPane else redPane
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (slot.stack?.metadata == 14) slot.putStack(greenPane) else slot.putStack(redPane)
        mc.thePlayer.playSound("random.orb", 1f, 1f)
        TerminalSolver.onGuiLoad(GuiLoadedEvent(name, inventorySlots as ContainerChest))
        if (inventorySlots.inventorySlots.subList(0, 45).none { it?.stack?.metadata == 14 }) {
            solved()
        }
    }
}
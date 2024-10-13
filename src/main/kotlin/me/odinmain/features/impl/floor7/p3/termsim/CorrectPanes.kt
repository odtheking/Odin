package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSounds
import me.odinmain.features.impl.floor7.p3.TerminalSounds.playTerminalSound
import me.odinmain.utils.postAndCatch
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
        cleanInventory()
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
        if (TerminalSounds.enabled) playTerminalSound() else mc.thePlayer.playSound("random.orb", 1f, 1f)
        GuiEvent.Loaded(name, inventorySlots as ContainerChest).postAndCatch()
        if (inventorySlots.inventorySlots.subList(0, 45).none { it?.stack?.metadata == 14 })
            solved(this.name, 0)
    }

    override fun onGuiClosed() {
        resetInv()
        super.onGuiClosed()
    }
}
package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSounds
import me.odinmain.features.impl.floor7.p3.TerminalSounds.clickSounds
import me.odinmain.utils.postAndCatch
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object Rubix : TermSimGui(
    "Change all to same color!",
    45
) {
    private val order = listOf(1, 4, 13, 11, 14)
    private val grid get() = inventorySlots.inventorySlots.subList(0, 45).filter { it?.stack?.metadata != 15 && it.stack.item != pane }

    override fun create() {
        cleanInventory()
        this.inventorySlots.inventorySlots.subList(0, 45).forEachIndexed { index, it ->
            if (floor(index / 9.0) in 1.0..3.0 && index % 9 in 3..5) it.putStack(getPane())
            else it.putStack(blackPane)
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (slot.stack?.metadata !in order) return
        val current = order.find { it == slot.stack.metadata } ?: return
        when (button) {
            0, 2 -> slot.putStack(genStack(order[(order.indexOf(current) + 1) % order.size]))
            1 -> {
                val nextIndex = order.indexOf(current) - 1
                slot.putStack(if (nextIndex < 0) genStack(order.last()) else genStack(order[nextIndex]))
            }
            else -> return
        }
        if (!TerminalSounds.enabled || !clickSounds) mc.thePlayer?.playSound("random.orb", 1f, 1f)
        GuiEvent.GuiLoadedEvent(name, inventorySlots as ContainerChest).postAndCatch()
        if (grid.all { it?.stack?.metadata == grid.firstOrNull()?.stack?.metadata })
            solved(this.name, 1)
    }

    private fun getPane(): ItemStack {
        val a = Math.random()
        return when {
            a < .2 -> genStack(order[0])
            a < .4 -> genStack(order[1])
            a < .6 -> genStack(order[2])
            a < .8 -> genStack(order[3])
            else ->   genStack(order[4])
        }
    }

    override fun onGuiClosed() {
        resetInv()
        super.onGuiClosed()
    }

    private fun genStack(meta: Int) = ItemStack(pane, 1, meta).apply { setStackDisplayName("") } // This makes unique itemstacks, so terminalsolver works.
}
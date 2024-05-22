package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalTimes
import me.odinmain.utils.postAndCatch
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object InOrder : TermSimGui(
    "Click in order!",
    36
) {
    override fun create() {
        val used = (1..14).shuffled().toMutableList()
        inventorySlots.inventorySlots.subList(0, size).forEachIndexed { index, it ->
            if (floor(index / 9.0) in 1.0..2.0 && index % 9 in 1..7) {
                it.putStack(ItemStack(pane, used.first(), 14).apply { setStackDisplayName("") })
                used.removeFirst()
            }
            else it.putStack(blackPane)
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (
            inventorySlots.inventorySlots
                .subList(0, size)
                .filter { it.stack?.metadata == 14 }
                .minByOrNull { it.stack?.stackSize ?: 999 } != slot
        ) return
        slot.putStack(ItemStack(pane, slot.stack.stackSize, 5).apply { setStackDisplayName("") })
        mc.thePlayer.playSound("random.orb", 1f, 1f)
        GuiEvent.GuiLoadedEvent(name, inventorySlots as ContainerChest).postAndCatch()
        if (inventorySlots.inventorySlots.subList(0, size).none { it?.stack?.metadata == 14 }) {
            solved(this.name, 2)
        }
    }
}
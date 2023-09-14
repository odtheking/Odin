package me.odinclient.features.impl.floor7.p3.termsim

import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object StartGui : TermSimGui(
    "Terminal Simulator",
    27
) {
    private val dye = Item.getItemById(351)
    private val termItems = listOf<ItemStack>(
        ItemStack(dye, 1, 10).setStackDisplayName("§aCorrect all the panes!"),
        ItemStack(dye, 1, 14).setStackDisplayName("§6Change all to same color!"),
        ItemStack(dye, 1, 6).setStackDisplayName("§3Click in order!"),
        ItemStack(dye, 1, 5).setStackDisplayName("§5What starts with: \"*\"?"),
        ItemStack(dye, 1, 12).setStackDisplayName("§bSelect all the \"*\" items!")
    )

    override fun create() {
        this.inventorySlots.inventorySlots.subList(0, 27).forEachIndexed { index, it ->
            if (index in 11..15) it.putStack(termItems[index - 11])
            else it.putStack(blackPane)
        }
    }

    override fun slotClick(slot: Slot) {
        if (slot.slotIndex !in 11..15) return
        when (slot.slotIndex) {
            11 -> CorrectPanes.open()
            /*
            12 -> SameColor.open()
            13 -> InOrder.open()
            14 -> StartsWith.open()
            15 -> Select.open()
             */
        }
    }
}
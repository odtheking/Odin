package me.odinclient.features.impl.floor7.p3.termsim

import me.odinclient.OdinClient.Companion.display
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object StartGui : GuiChest(
    mc.thePlayer.inventory,
    InventoryBasic("Terminal Simulator", true, 27)
) {
    private val pane = Item.getItemById(160)
    private val blackPane = ItemStack(pane, 1, 15)
    private val dye = Item.getItemById(351)
    private val termItems = listOf<ItemStack>(
        ItemStack(dye, 1, 10).setStackDisplayName("§aCorrect all the panes!"),
        ItemStack(dye, 1, 14).setStackDisplayName("§6Change all to same color!"),
        ItemStack(dye, 1, 6).setStackDisplayName("§3Click in order!"),
        ItemStack(dye, 1, 5).setStackDisplayName("§5What starts with"),
        ItemStack(dye, 1, 12).setStackDisplayName("§bSelect all the")
    )

    init {
        this.inventorySlots.inventorySlots.subList(0, 27).forEach {
            it.putStack(blackPane)
        }
    }

    fun open() {
        display = this
        this.inventorySlots.inventorySlots.subList(0, 27).forEachIndexed { index, it ->
            if (index in 11..15) it.putStack(termItems[index - 11])
            else it.putStack(blackPane)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return
        val slot = slotUnderMouse
        if (slot.slotIndex !in 11..15) return
        modMessage("clicked lil nigga")
    }
}
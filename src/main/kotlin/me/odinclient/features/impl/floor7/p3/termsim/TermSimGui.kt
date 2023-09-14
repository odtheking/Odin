package me.odinclient.features.impl.floor7.p3.termsim

import me.odinclient.OdinClient.Companion.display
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

open class TermSimGui(name: String, val size: Int) : GuiChest(
    mc.thePlayer.inventory,
    InventoryBasic(name, true, size)
) {
    val pane: Item = Item.getItemById(160)
    val blackPane = ItemStack(pane, 1, 15).apply { setStackDisplayName("") }
    private var startTime = 0L

    open fun create() {
        this.inventorySlots.inventorySlots.subList(0, size).forEach { it.putStack(blackPane) } // override
    }

    fun open() {
        create()
        display = this
        startTime = System.currentTimeMillis()
        modMessage("set time")
    }

    fun solved() {
        modMessage("§aTerminal solved in §6${(System.currentTimeMillis() - startTime) / 1000.0}s §a!")
        display = StartGui
    }

    open fun slotClick(slot: Slot) {}

    final override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val slot = slotUnderMouse ?: return
        if (slot.stack?.metadata == 15) return
        slotClick(slot)
    }
}
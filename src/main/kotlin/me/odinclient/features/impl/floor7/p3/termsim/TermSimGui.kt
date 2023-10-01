package me.odinclient.features.impl.floor7.p3.termsim

import me.odinclient.OdinClient.Companion.display
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.GuiClosedEvent
import me.odinclient.events.impl.GuiLoadedEvent
import me.odinclient.features.impl.floor7.p3.TerminalSolver
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

open class TermSimGui(val name: String, val size: Int) : GuiChest(
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
        TerminalSolver.onGuiLoad(GuiLoadedEvent(name, inventorySlots as ContainerChest))
    }

    fun solved() {
        modMessage("§aTerminal solved in §6${(System.currentTimeMillis() - startTime) / 1000.0}s §a!")
        StartGui.open()
    }

    open fun slotClick(slot: Slot, button: Int) {}

    final override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val slot = slotUnderMouse ?: return
        if (slot.stack?.item == pane && slot.stack?.metadata == 15) return
        slotClick(slot, mouseButton)
        inventorySlots
    }
}
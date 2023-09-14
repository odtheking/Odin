package me.odinclient.features.impl.floor7.p3.termsim

import me.odinclient.OdinClient
import me.odinclient.OdinClient.Companion.display
import me.odinclient.features.impl.floor7.p3.termsim.StartGui.blackPane
import me.odinclient.features.impl.floor7.p3.termsim.StartGui.pane
import me.odinclient.utils.Utils.round
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object CorrectPanes : TermSimGui(
    "Correct The Panes!", 45
) {
    private val greenPane = ItemStack(pane, 1, 5).apply { setStackDisplayName("") }
    private val redPane = ItemStack(pane, 1, 14).apply { setStackDisplayName("") }

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

    override fun slotClick(slot: Slot) {
        if (slot.stack.metadata != 14) return
        slot.putStack(greenPane)
        mc.thePlayer.playSound("random.orb", 1f, 1f)
        if (inventorySlots.inventorySlots.subList(0, 45).none { it?.stack?.metadata == 14 }) {
            solved()
        }
    }
}
package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.floor

object NumbersSim : TermSimGUI(
    TerminalTypes.NUMBERS.termName, TerminalTypes.NUMBERS.windowSize
) {
    override fun create() {
        val used = (1..14).shuffled().toMutableList()
        createNewGui {
            if (floor(it.index / 9f) in 1f..2f && it.index % 9 in 1..7) ItemStack(Items.RED_STAINED_GLASS_PANE, used.first()).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§a${used.removeFirst()}")) }
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (guiInventorySlots.minByOrNull { if (it?.item?.item == Items.RED_STAINED_GLASS_PANE) it.item?.count ?: 999 else 1000 } != slot) return
        createNewGui {
            if (it == slot) ItemStack(Items.LIME_STAINED_GLASS_PANE, slot.item.count).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
            else it.item ?: blackPane
        }
        playTermSimSound()
        if (guiInventorySlots.none { it?.item?.item == Items.RED_STAINED_GLASS_PANE })
            TerminalUtils.lastTermOpened?.onComplete()
    }
}
package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.floor

object PanesSim : TermSimGUI(
    TerminalTypes.PANES.termName, TerminalTypes.PANES.windowSize
) {
    private val greenPane get() = ItemStack(Items.LIME_STAINED_GLASS_PANE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
    private val redPane   get() = ItemStack(Items.RED_STAINED_GLASS_PANE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }

    override fun create() {
        createNewGui {
            if (floor(it.index / 9f) in 1f..3f && it.index % 9 in 2..6) if (Math.random() > 0.75) greenPane else redPane else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        createNewGui { if (it == slot) { if (slot.item?.item == Items.RED_STAINED_GLASS_PANE) greenPane else redPane } else it.item }

        playTermSimSound()
        if (guiInventorySlots.none { it?.item?.item == Items.RED_STAINED_GLASS_PANE })
            TerminalUtils.lastTermOpened?.onComplete()
    }
}
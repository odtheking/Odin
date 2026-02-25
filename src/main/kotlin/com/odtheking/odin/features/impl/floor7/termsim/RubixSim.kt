package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.StainedGlassPaneBlock
import kotlin.math.floor

object RubixSim : TermSimGUI(
    TerminalTypes.RUBIX.termName, TerminalTypes.RUBIX.windowSize
) {
    private val order = listOf(DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED)
    private val panes = listOf(Items.ORANGE_STAINED_GLASS_PANE, Items.YELLOW_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE, Items.BLUE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE)
    private val indices = listOf(12, 13, 14, 21, 22, 23, 30, 31, 32)

    override fun create() {
        createNewGui {
            if (floor(it.index / 9f) in 1f..3f && it.index % 9 in 3..5) getPane()
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        val current = order.find { it == ((slot.item?.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color } ?: return
        createNewGui {
            if (it == slot) {
                if (button == 1) genStack(order.indexOf(current) - 1)
                else genStack((order.indexOf(current) + 1) % order.size)
            } else it.item ?: blackPane
        }

        playTermSimSound()
        if (indices.all { guiInventorySlots[it]?.item?.item == guiInventorySlots[12]?.item?.item })
            TerminalUtils.lastTermOpened?.onComplete()
    }

    private fun getPane(): ItemStack {
        return when (Math.random()) {
            in 0.0..0.2 -> genStack(0)
            in 0.2..0.4 -> genStack(1)
            in 0.4..0.6 -> genStack(2)
            in 0.6..0.8 -> genStack(3)
            else -> genStack(4)
        }
    }

    private fun genStack(meta: Int): ItemStack =
        ItemStack(panes[if (meta in panes.indices) meta else panes.lastIndex]).apply { set(DataComponents.CUSTOM_NAME, Component.literal("")) }
}
package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class StartsWithHandler(private val letter: String): TerminalHandler(TerminalTypes.STARTS_WITH) {

    private val clickedSlots = mutableSetOf<Int>()

    private var clickedSlot: Pair<Int, Int>? = null

    override fun solve(items: List<ItemStack>): List<Int> {
        clickedSlot?.let {
            val screenHandler = (mc.screen as? ContainerScreen)?.menu
            if (it.first != screenHandler?.containerId) {
                val item = items[it.second].item
                if (item == Items.NETHER_STAR || item == Items.EXPERIENCE_BOTTLE) clickedSlots.add(it.second)
                clickedSlot = null
            }
        }

        return items.mapIndexedNotNull { index, item ->
            if (item.hoverName.string.startsWith(letter, true) && !item.hasGlint() && index !in clickedSlots) index else null
        }
    }

    override fun click(slotIndex: Int, button: Int, simulateClick: Boolean) {
        val screenHandler = (mc.screen as? ContainerScreen)?.menu ?: return
        if (canClick(slotIndex, button) && clickedSlot == null)
            clickedSlot = screenHandler.containerId to slotIndex

        super.click(slotIndex, button, simulateClick)
    }

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = TerminalSolver.startsWithColor to null
}
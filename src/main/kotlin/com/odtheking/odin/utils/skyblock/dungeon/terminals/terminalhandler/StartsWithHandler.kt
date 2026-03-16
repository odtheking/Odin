package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.item.ItemStack

class StartsWithHandler(private val letter: String): TerminalHandler(TerminalTypes.STARTS_WITH) {

    private val clickedSlots = mutableSetOf<Int>()
    private var lastContainerId = -1

    override fun solve(items: List<ItemStack>): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (item.hoverName?.string?.startsWith(letter, true) == true && !item.hasGlint() && index !in clickedSlots) index else null
        }
    }

    override fun click(slotIndex: Int, button: Int, simulateClick: Boolean) {
        val screenHandler = (mc.screen as? ContainerScreen)?.menu ?: return
        if (canClick(slotIndex, button) && lastContainerId != screenHandler.containerId) {
            clickedSlots.add(slotIndex)
            lastContainerId = screenHandler.containerId
        }
        super.click(slotIndex, button, simulateClick)
    }
}
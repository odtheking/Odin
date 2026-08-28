package com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler

import com.odtheking.odin.features.impl.boss.TerminalSolver
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Items

class StartsWithHandler(private val letter: String): TerminalHandler(TerminalTypes.STARTS_WITH) {

    private val clickedOverrides = HashMap<Int, Boolean>()

    override fun solve(slots: List<Slot>, updatedIndex: Int): List<Int> {
        clickedOverrides[updatedIndex] = true
        return slots.mapIndexedNotNull { index, slot ->
            if (
                slot.item.hoverName.string.startsWith(letter, true) &&
                clickedOverrides[index] == true &&
                (!slot.item.hasGlint() || slot.item.item in enchantOverrides)
            ) index else null
        }
    }

    override fun click(slotIndex: Int, button: Int, simulateClick: Boolean) {
        if (canClick(slotIndex, button) && slotIndex !in clickedOverrides)
            clickedOverrides[slotIndex] = false

        super.click(slotIndex, button, simulateClick)
    }

    override fun renderSlot(slotIndex: Int): Pair<Color, String?> = TerminalSolver.startsWithColor to null

    private companion object {
        val enchantOverrides = BuiltInRegistries.ITEM.filter { it.components().has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE) } + Items.GOLDEN_APPLE
    }
}

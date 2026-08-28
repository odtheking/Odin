package com.odtheking.odin.features.impl.boss.termsim

import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.floor

class StartsWithSim(private val letter: String = listOf("A", "B", "C", "G", "D", "M", "N", "R", "S", "T", "W").random()) : TermSimGUI(
    "What starts with: \'$letter\'?",
    TerminalTypes.STARTS_WITH.windowSize
) {
    private val clickedOverrides = ArrayList<Int>()

    override fun create() {
        setSlots {
            when {
                floor(it.index / 9f) !in 1f..3f || it.index % 9 !in 1..7 -> blackPane
                it.index == (10..16).random() -> getLetterItemStack()
                Math.random() > .7f -> getLetterItemStack()
                else -> getLetterItemStack(true)
            }
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (!slot.item.hoverName.string.startsWith(letter, true)) return modMessage("§cThat item does not start with: \'$letter\'!")
        if (slot.item.hasRealGlint() || slot.index in clickedOverrides) return modMessage("§cAlready selected!")

        if (slot.item.item in enchantOverrides) clickedOverrides.add(slot.index)
        slot.setSlot(slot.item.apply { set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true) })

        if (guiInventorySlots.all { !it.item.hoverName.string.startsWith(letter, true) || it.item.hasRealGlint() || it.index in clickedOverrides })
            TerminalUtils.lastTermOpened?.onComplete()

        super.slotClick(slot, button)
    }

    private fun getLetterItemStack(filterNot: Boolean = false): ItemStack {
        val matchingItem = BuiltInRegistries.ITEM
            .filter { item ->
                val id = BuiltInRegistries.ITEM.getKey(item).path
                id.startsWith(letter, true) != filterNot && !id.contains("pane", true) && item != Items.AIR
            }.randomOrNull() ?: return ItemStack.EMPTY

        return ItemStack(matchingItem)
    }

    private companion object {
        fun ItemStack.hasRealGlint(): Boolean = hasGlint() && item !in enchantOverrides
        val enchantOverrides = BuiltInRegistries.ITEM.filter { it.components().has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE) } + Items.GOLDEN_APPLE
    }
}
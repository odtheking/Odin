package com.odtheking.odin.features.impl.boss.termsim

import com.odtheking.odin.utils.hasGlint
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.floor

class SelectAllSim(
    private val color: DyeColor = DyeColor.entries.random()
) : TermSimGUI(
    "Select all the ${color.name.replace("LIGHT_GRAY", "SILVER").replace("_", " ")} items!",
    TerminalTypes.SELECT.windowSize
) {
    override fun create() {
        val guaranteed = (10..16).plus(19..25).plus(28..34).plus(37..43).random()
        createNewGui { slot ->
            if (floor(slot.index / 9.0) in 1.0..4.0 && slot.index % 9 in 1..7) {
                val item = ItemStack(getPossibleItems(color).random())

                if (slot.index == guaranteed) item
                else {
                    if (Math.random() > 0.75) item
                    else ItemStack(getPossibleItems(DyeColor.entries.filter { it != color }.random()).random())
                }
            } else blackPane
        }
    }

    private fun getPossibleItems(color: DyeColor): List<Item> {
        return listOf(
            BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("minecraft", "${color.name.lowercase()}_stained_glass")).get().value(),
            BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("minecraft", "${color.name.lowercase()}_wool")).get().value(),
            BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("minecraft", "${color.name.lowercase()}_concrete")).get().value(),
            when (color) {
                DyeColor.WHITE -> Items.BONE_MEAL
                DyeColor.BLUE -> Items.LAPIS_LAZULI
                DyeColor.BLACK -> Items.INK_SAC
                DyeColor.BROWN -> Items.COCOA_BEANS
                else -> BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("minecraft", "${color.name.lowercase()}_dye")).get().value()
            }
        )
    }

    override fun slotClick(slot: Slot, button: Int) {
        val possibleItems = getPossibleItems(color)
        if (!possibleItems.contains(slot.item.item)) return modMessage("§cThat item is not: ${color.name.uppercase()}!")

        createNewGui {
            if (it == slot) slot.item.apply { set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true) }
            else it.item
        }

        playTermSimSound()

        if (guiInventorySlots.none { !it.item.hasGlint() && possibleItems.contains(it.item.item) })
            TerminalUtils.lastTermOpened?.onComplete()
    }
}

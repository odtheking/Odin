package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import kotlin.math.floor

class SelectAllSim(private val color: String = EnumDyeColor.entries.random().name.replace("_", " ").uppercase()) : TermSimGUI(
    "Select all the $color items!",
    TerminalTypes.SELECT.windowSize
) {
    private val correctMeta = EnumDyeColor.entries.find { it.name.replace("_", " ").uppercase() == color }?.metadata ?: 0
    private val correctDye = EnumDyeColor.byMetadata(correctMeta).dyeDamage
    private val clay = Item.getItemById(159)
    private val glass = Item.getItemById(95)
    private val wool = Item.getItemById(35)
    private val dye = Item.getItemById(351)
    private val items = listOf(clay, glass, wool, dye)

    override fun create() {
        val guaranteed = (10..16).plus(19..25).plus(28..34).plus(37..43).random()
        createNewGui {
            if (floor(it.slotIndex / 9.0) in 1.0..4.0 && it.slotIndex % 9 in 1..7) {
                val item = items.random()
                if (it.slotIndex == guaranteed)
                    if (item == dye) ItemStack(item, 1, correctDye) else ItemStack(item, 1, correctMeta)
                else {
                    if (Math.random() > .75) {
                        if (item == dye) ItemStack(item, 1, correctDye)
                        else ItemStack(item, 1, correctMeta)
                    } else ItemStack(items.random(), 1, EnumDyeColor.entries.filter { dye -> dye.metadata != correctMeta }.random().metadata)
                }
            }
            else blackPane
        }
    }

    override fun slotClick(slot: Slot, button: Int) = with(slot.stack) {
        if (isItemEnchanted || item !in items || item == dye && metadata != correctDye || item != dye && metadata != correctMeta) {
            mc.thePlayer?.closeScreen()
            return modMessage("§cThat item is not: $color!")
        }

        createNewGui { if (it == slot) ItemStack(item, stackSize, metadata).apply { addEnchantment(Enchantment.infinity, 1) } else it.stack }
        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.isItemEnchanted == false && it.stack?.item in items && if (it.stack?.item == dye) it.stack?.metadata == correctDye else it.stack?.metadata == correctMeta })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }
}
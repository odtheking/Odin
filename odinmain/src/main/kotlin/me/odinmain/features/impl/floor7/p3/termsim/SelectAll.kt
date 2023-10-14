package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.getRandom
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import kotlin.math.floor

class SelectAll(private val color: String) : TermSimGui(
    "Select all the $color items!",
    54
) {
    private val correctMeta = EnumDyeColor.entries.find { it.name.replace("_", " ").uppercase() == color }?.metadata ?: 0
    private val correctDye = EnumDyeColor.byMetadata(correctMeta).dyeDamage
    private val clay = Item.getItemById(159)
    private val glass = Item.getItemById(95)
    private val wool = Item.getItemById(35)
    private val dye = Item.getItemById(351)
    private val items = listOf(clay, glass, wool, dye)

    override fun create() {
        val guaranteed = (10..16).plus(19..25).plus(28..34).plus(37..43).getRandom()
        inventorySlots.inventorySlots.subList(0, size).forEachIndexed { index, it ->
            if (floor(index / 9.0) in 1.0..4.0 && index % 9 in 1..7) {
                val item = items.getRandom()
                if (index == guaranteed) {
                    if (item == dye) it.putStack(ItemStack(item, 1, correctDye)) else it.putStack(ItemStack(item, 1, correctMeta))
                } else {
                    val shouldBeCorrect = Math.random() > .75
                    if (shouldBeCorrect) {
                        if (item == dye) it.putStack(ItemStack(item, 1, correctDye))
                        else it.putStack(ItemStack(item, 1, correctMeta))
                    } else it.putStack(ItemStack(items.getRandom(), 1, EnumDyeColor.entries.filter { it.metadata != correctMeta }.getRandom().metadata))
                }
            }
            else it.putStack(blackPane)
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (
            slot.stack?.isItemEnchanted == true ||
            slot.stack?.item !in items ||
            slot.stack?.item == dye && slot.stack?.metadata != correctDye ||
            slot.stack?.item != dye && slot.stack?.metadata != correctMeta
        ) return

        slot.stack.addEnchantment(Enchantment.infinity, 1)
        mc.thePlayer.playSound("random.orb", 1f, 1f)
        TerminalSolver.onGuiLoad(GuiLoadedEvent(name, inventorySlots as ContainerChest))
        if (inventorySlots.inventorySlots.subList(0, size).none {
                it.stack?.isItemEnchanted == false && it.stack?.item in items && if (it.stack?.item == dye) it.stack?.metadata == correctDye else it.stack?.metadata == correctMeta
            }) solved()
    }
}
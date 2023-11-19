package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTimes
import me.odinmain.utils.getRandom
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.registry.GameData
import kotlin.math.floor

class StartsWith(private val letter: String) : TermSimGui(
    "What starts with: \'$letter\'?",
    54
) {
    override fun create() {
        val guaranteed = (10..16).getRandom()
        inventorySlots.inventorySlots.subList(0, size).forEachIndexed { index, it ->
            if (floor(index / 9.0) in 1.0..4.0 && index % 9 in 1..7) {
                if (index == guaranteed) {
                    it.putStack(ItemStack(
                        GameData.getItemRegistry().filter { it.registryName.replace("minecraft:", "").startsWith(letter, true) }.getRandom()
                    ))
                    return@forEachIndexed
                }
                val shouldBeCorrect = Math.random() > .7
                if (shouldBeCorrect)
                    it.putStack(ItemStack(
                        GameData.getItemRegistry().filter { it.registryName.replace("minecraft:", "").startsWith(letter, true) }.getRandom()
                    ))
                else
                    it.putStack(ItemStack(
                        GameData.getItemRegistry().filterNot { it.registryName.replace("minecraft:", "").startsWith(letter, true) }.getRandom()
                    ))
            }
            else it.putStack(blackPane)
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (!slot.stack.displayName.startsWith(letter, true) || slot.stack.isItemEnchanted) return

        slot.stack.addEnchantment(Enchantment.infinity, 1)
        mc.thePlayer.playSound("random.orb", 1f, 1f)
        TerminalSolver.onGuiLoad(GuiLoadedEvent(name, inventorySlots as ContainerChest))
        if (inventorySlots.inventorySlots.subList(0, size).none { it.stack.displayName.startsWith(letter, true) && !it.stack.isItemEnchanted }) {
            solved(this.name, TerminalTimes.simStartsWithPB)
        }
    }

    companion object {
        val letters = listOf("A", "B", "C", "G", "D", "M", "N", "R", "S", "T")
    }
}
package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.impl.floor7.p3.TerminalSounds
import me.odinmain.features.impl.floor7.p3.TerminalSounds.clickSounds
import me.odinmain.utils.getRandom
import me.odinmain.utils.postAndCatch
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
        val slot = slot.stack ?: return
        if (slot.displayName?.startsWith(letter, true) == false || slot.isItemEnchanted) return

        slot.addEnchantment(Enchantment.infinity, 1)
        if (!TerminalSounds.enabled || !clickSounds) mc.thePlayer.playSound("random.orb", 1f, 1f)
        GuiEvent.GuiLoadedEvent(name, inventorySlots as ContainerChest).postAndCatch()
        if (inventorySlots?.inventorySlots?.subList(0, size)?.none { it?.stack?.displayName?.startsWith(letter, true) == true && !it.stack.isItemEnchanted } == true) {
            solved(this.name, 3)
        }
    }

    companion object {
        val letters = listOf("A", "B", "C", "G", "D", "M", "N", "R", "S", "T")
    }
}
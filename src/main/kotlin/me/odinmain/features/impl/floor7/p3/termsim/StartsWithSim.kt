package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.registry.GameData
import kotlin.math.floor

class StartsWithSim(private val letter: String = listOf("A", "B", "C", "G", "D", "M", "N", "R", "S", "T").random()) : TermSimGUI(
    "What starts with: \'$letter\'?",
    TerminalTypes.STARTS_WITH.windowSize
) {
    override fun create() {
        createNewGui {
            when {
                floor(it.slotIndex / 9.0) !in 1.0..3.0 || it.slotIndex % 9 !in 1..7 -> blackPane
                it.slotIndex == (10..16).random() -> getLetterItemStack()
                Math.random() > .7 -> getLetterItemStack()
                else -> getLetterItemStack(true)
            }
        }
    }

    override fun slotClick(slot: Slot, button: Int) = with(slot.stack) {
        if (displayName?.startsWith(letter, true) == false || isItemEnchanted) {
            mc.thePlayer?.closeScreen()
            return modMessage("§cThat item does not start with: \'$letter\'!")
        }

        createNewGui { if (it == slot) ItemStack(item, stackSize, metadata).apply { addEnchantment(Enchantment.infinity, 1) } else it.stack }
        playTermSimSound()
        if (guiInventorySlots.none { it?.stack?.displayName?.startsWith(letter, true) == true && !it.stack.isItemEnchanted })
            TerminalSolver.lastTermOpened?.let { TerminalEvent.Solved(it).postAndCatch() }
    }

    private fun getLetterItemStack(filterNot: Boolean = false): ItemStack =
        ItemStack(GameData.getItemRegistry().filter { it.registryName.replace("minecraft:", "").startsWith(letter, true) != filterNot }.random())
}
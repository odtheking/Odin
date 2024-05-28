package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.features.impl.floor7.p3.TerminalTimes
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.setLoreWidth
import net.minecraft.inventory.Slot
import net.minecraft.item.*
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object StartGui : TermSimGui(
    "Terminal Simulator",
    27
) {
    private val dye = Item.getItemById(351)
    private val termItems = listOf(
        ItemStack(dye, 1, 10).setStackDisplayName("§aCorrect all the panes!"),
        ItemStack(dye, 1, 14).setStackDisplayName("§6Change all to same color!"),
        ItemStack(dye, 1, 6).setStackDisplayName("§3Click in order!"),
        ItemStack(dye, 1, 5).setStackDisplayName("§5What starts with: \"*\"?"),
        ItemStack(dye, 1, 12).setStackDisplayName("§bSelect all the \"*\" items!")
    )
    private val resetButton = ItemStack(dye, 1, 8).setStackDisplayName("§cReset PBs")
    private val randomButton = ItemStack(dye, 1, 15).setStackDisplayName("§7Random")
    private val redstoneTorch = ItemStack(Item.getItemById(76), 1).setStackDisplayName("§4Common issues").setLoreWidth(listOf("§7- One of your mods might be §cconflicting §7with §5Termsim§7", "§7Issues that may be caused by others mods are item stacking, §7duping, glitching and more", "§7- Terminal solver from §cincompatible mods§7 won't work for §5Termsim", "§7- We recommend using odin's terminal solver for everything!", "§6If you have any issues with termsim, please report it to the §6discord server!"), 67)

    override fun create() {
        this.inventorySlots.inventorySlots.subList(0, 27).forEachIndexed { index, it ->
            when (index) {
                4 -> it.putStack(resetButton)
                in 11..15 -> it.putStack(termItems[index - 11])
                22 -> it.putStack(randomButton)
                26 -> it.putStack(redstoneTorch)
                else -> it.putStack(blackPane)
            }
        }
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (event.itemStack.item != dye || event.toolTip.size == 0) return
        val index = termItems.indexOfFirst { it.displayName == event.itemStack.displayName }.takeIf { it != -1 } ?: return
        event.toolTip.add(1, "§7Personal Best: §d${TerminalTimes.simPBs.pb?.get(index)?.round(2) ?: 999.0}")
    }

    private var areYouSure = false

    override fun slotClick(slot: Slot, button: Int) {
        val index = if (slot.slotIndex == 22) listOf(11,12,13,14,15).getRandom() else slot.slotIndex
        when (index) {
            4 -> {
                if (!areYouSure) {
                    modMessage("§cAre you sure you want to reset your PBs? Click again to confirm.")
                    areYouSure = true
                    runIn(60) {
                        modMessage("§aPBs reset cancelled.")
                        areYouSure = false
                    }
                    return
                }
                repeat(5) { i -> TerminalTimes.simPBs.set(i, 999.0) }
                modMessage("§cPBs reset!")
                StartGui.open(ping)
            }
            11 -> CorrectPanes.open(ping)
            12 -> Rubix.open(ping)
            13 -> InOrder.open(ping)
            14 -> StartsWith(StartsWith.letters.shuffled().first()).open(ping)
            15 -> SelectAll(EnumDyeColor.entries.getRandom().name.replace("_", " ").uppercase()).open(ping)
        }
    }
}
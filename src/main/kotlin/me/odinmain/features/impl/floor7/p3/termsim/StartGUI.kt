package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.features.impl.floor7.TerminalSimulator
import me.odinmain.features.impl.floor7.TerminalSimulator.openRandomTerminal
import me.odinmain.utils.round
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.setLoreWidth
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object StartGUI : TermSimGUI(
    "Terminal Simulator", 27
) {
    private val dye = Item.getItemById(351)
    private val termItems = listOf(
        ItemStack(dye, 1, 10).setStackDisplayName("§aCorrect all the panes!"),
        ItemStack(dye, 1, 14).setStackDisplayName("§6Change all to same color!"),
        ItemStack(dye, 1, 6).setStackDisplayName("§3Click in order!"),
        ItemStack(dye, 1, 5).setStackDisplayName("§5What starts with: \"*\"?"),
        ItemStack(dye, 1, 12).setStackDisplayName("§bSelect all the \"*\" items!"),
        ItemStack(dye, 1, 9).setStackDisplayName("§dClick the button on time!")
    )
    private val resetButton = ItemStack(dye, 1, 8).setStackDisplayName("§cReset PBs")
    private val randomButton = ItemStack(dye, 1, 15).setStackDisplayName("§7Random")
    private val redstoneTorch = ItemStack(Item.getItemById(76), 1).setStackDisplayName("§4Common issues").setLoreWidth(listOf("§7- One of your mods might be §cconflicting §7with §5Termsim§7", "§7Issues that may be caused by others mods are item stacking, §7duping, glitching and more", "§7- Terminal solver from §cincompatible mods§7 won't work for §5Termsim", "§7- We recommend using odin's terminal solver for everything!", "§6If you have any issues with termsim, please report it to the §6discord server!"), 67)

    override fun create() {
        createNewGui {
            when (it.slotIndex) {
                4 -> resetButton
                13  -> randomButton
                in 10..12 -> termItems[it.slotIndex - 10]
                in 14..16 -> termItems[it.slotIndex - 11]
                22 -> redstoneTorch
                else -> blackPane
            }
        }
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (event.itemStack.item != dye || event.toolTip.isEmpty()) return
        val index = termItems.indexOfFirst { it.displayName == event.itemStack?.displayName }.takeIf { it != -1 } ?: return
        event.toolTip.add(1, "§7Personal Best: §d${TerminalSimulator.termSimPBs.pb?.get(index)?.round(2) ?: 999.0}")
    }

    private var areYouSure = false

    override fun slotClick(slot: Slot, button: Int) {
        when (slot.slotIndex) {
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
                repeat(6) { i -> TerminalSimulator.termSimPBs.set(i, 999.0) }
                modMessage("§cPBs reset!")
            }
            10 -> PanesSim.open(ping)
            11 -> RubixSim.open(ping)
            12 -> NumbersSim.open(ping)
            13 -> openRandomTerminal(ping)
            14 -> StartsWithSim().open(ping)
            15 -> SelectAllSim().open(ping)
            16 -> MelodySim.open(ping)
        }
    }
}
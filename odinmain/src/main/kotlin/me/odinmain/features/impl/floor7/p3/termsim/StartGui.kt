package me.odinmain.features.impl.floor7.p3.termsim

import me.odinmain.config.Config
import me.odinmain.features.impl.floor7.p3.TerminalTimes
import me.odinmain.utils.getRandom
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
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
    private val pbTimes = listOf(
        TerminalTimes.simPanesPB,
        TerminalTimes.simColorPB,
        TerminalTimes.simNumbersPB,
        TerminalTimes.simStartsWithPB,
        TerminalTimes.simSelectAllPB
    )

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun create() {
        this.inventorySlots.inventorySlots.subList(0, 27).forEachIndexed { index, it ->
            when (index) {
                4 -> it.putStack(resetButton)
                in 11..15 -> it.putStack(termItems[index - 11])
                22 -> it.putStack(randomButton)
                else -> it.putStack(blackPane)
            }
        }
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (event.itemStack.item != dye || event.toolTip.size == 0) return
        val index = termItems.indexOfFirst { it.displayName == event.itemStack.displayName }.takeIf { it != -1 } ?: return
        event.toolTip.add(1, "§7Personal Best: §d${pbTimes[index].value.round(2)}")
    }

    override fun slotClick(slot: Slot, button: Int) {
        val terms = listOf(
            CorrectPanes,
            SameColor,
            InOrder,
            StartsWith(StartsWith.letters.shuffled().first()),
            SelectAll(EnumDyeColor.entries.getRandom().name.replace("_", " ").uppercase())
        )
        when (slot.slotIndex) {
            4 -> {
                pbTimes.forEach { it.value = 99.0 }
                Config.saveConfig()
                modMessage("§cPBs reset!")
                StartGui.open(ping)
            }
            11 -> terms[0].open(ping)
            12 -> terms[1].open(ping)
            13 -> terms[2].open(ping)
            14 -> terms[3].open(ping)
            15 -> terms[4].open(ping)
            22 -> terms.getRandom().open(ping)

        }
    }
}
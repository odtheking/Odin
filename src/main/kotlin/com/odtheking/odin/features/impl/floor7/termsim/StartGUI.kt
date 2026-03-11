package com.odtheking.odin.features.impl.floor7.termsim

import com.odtheking.odin.features.impl.floor7.TerminalSimulator
import com.odtheking.odin.features.impl.floor7.TerminalSimulator.openRandomTerminal
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes
import com.odtheking.odin.utils.toFixed
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore

object StartGUI : TermSimGUI(
    "Terminal Simulator", 27
) {
    private fun buildTermItem(type: TerminalTypes, item: Item, color: String) = ItemStack(item).apply {
        set(DataComponents.CUSTOM_NAME, Component.literal("$color${type.termName}"))
        set(DataComponents.LORE, ItemLore(getTermPB(type)))
    }

    private fun buildTermItems() = listOf(
        buildTermItem(TerminalTypes.PANES, Items.LIME_DYE, "§a"),
        buildTermItem(TerminalTypes.RUBIX, Items.RED_DYE, "§6"),
        buildTermItem(TerminalTypes.NUMBERS, Items.CYAN_DYE, "§3"),
        buildTermItem(TerminalTypes.STARTS_WITH, Items.PINK_DYE, "§5"),
        buildTermItem(TerminalTypes.SELECT, Items.BROWN_DYE, "§b"),
        buildTermItem(TerminalTypes.MELODY, Items.PURPLE_DYE, "§d")
    )

    private val resetButton = ItemStack(Items.BLACK_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§cReset PBs!")) }
    private val randomButton = ItemStack(Items.WHITE_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§7Random")) }

    fun getTermPB(type: TerminalTypes): List<Component> {
        val pb = TerminalSimulator.termSimPBs.get(type.name) ?: return listOf(Component.literal("§7No PB set!"))
        return listOf(Component.literal("§7Personal Best: §d${pb.toFixed()}s"),)
    }

    override fun create() {
        val termItems = buildTermItems()
        createNewGui {
            when (it.index) {
                4 -> resetButton
                13  -> randomButton
                in 10..12 -> termItems[it.index - 10]
                in 14..16 -> termItems[it.index - 11]
                else -> blackPane
            }
        }
    }

    private var areYouSure = false

    override fun slotClick(slot: Slot, button: Int) {
        when (slot.index) {
            4 -> {
                if (!areYouSure) {
                    modMessage("§cAre you sure you want to reset your PBs? Click again to confirm.")
                    areYouSure = true
                    schedule(60) {
                        if (!areYouSure) return@schedule
                        modMessage("§aPBs reset cancelled.")
                        areYouSure = false
                    }
                    return
                }
                TerminalSimulator.termSimPBs.reset()
                create()
                modMessage("§aPBs reset!")
                areYouSure = false
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
package com.odtheking.odin.features.impl.boss.termsim

import com.odtheking.odin.features.impl.boss.TerminalSimulator
import com.odtheking.odin.features.impl.boss.TerminalSimulator.openRandomTerminal
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object StartGUI : TermSimGUI(
    "Terminal Simulator", 27
) {
    private val termItems = listOf(
        ItemStack(Items.LIME_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§aCorrect all the panes!")) },
        ItemStack(Items.RED_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§6Change all to same color!")) },
        ItemStack(Items.CYAN_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§3Click in order!")) },
        ItemStack(Items.PINK_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§5What starts with: \"*\"?")) },
        ItemStack(Items.BROWN_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§bSelect all the \"*\" items!")) },
        ItemStack(Items.PURPLE_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§dClick the button on time!")) }
    )
    private val resetButton = ItemStack(Items.BLACK_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§cReset PBs!")) }
    private val randomButton = ItemStack(Items.WHITE_DYE).apply { set(DataComponents.CUSTOM_NAME, Component.literal("§7Random")) }

    override fun create() {
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
                    schedule(60,) {
                        if (!areYouSure) return@schedule
                        modMessage("§aPBs reset cancelled.")
                        areYouSure = false
                    }
                    return
                }
                TerminalSimulator.termSimPBs.reset()
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
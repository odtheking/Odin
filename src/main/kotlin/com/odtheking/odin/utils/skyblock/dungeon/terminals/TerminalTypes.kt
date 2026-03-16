package com.odtheking.odin.utils.skyblock.dungeon.terminals

import com.github.stivais.commodore.parsers.CommandParsable
import com.odtheking.odin.features.impl.floor7.termGUI.*
import com.odtheking.odin.features.impl.floor7.termsim.*
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.terminals.terminalhandler.*
import net.minecraft.world.item.DyeColor

@CommandParsable
enum class TerminalTypes(
    val termName: String,
    val regex: Regex,
    val windowSize: Int
) : Type {
    PANES("Correct all the panes!", Regex("^Correct all the panes!$"), 45) {
        override fun getSimulator() = PanesSim
        override fun getGUI() = PanesGui
    },
    RUBIX("Change all to same color!", Regex("^Change all to same color!$"), 45) {
        override fun getSimulator() = RubixSim
        override fun getGUI() = RubixGui
    },
    NUMBERS("Click in order!", Regex("^Click in order!$"), 36) {
        override fun getSimulator() = NumbersSim
        override fun getGUI() = NumbersGui
    },
    STARTS_WITH("What starts with:", Regex("^What starts with: '(\\w)'\\?$"), 45) {
        override fun getSimulator() = StartsWithSim()
        override fun getGUI() = StartsWithGui
    },
    SELECT("Select all the", Regex("^Select all the ([\\w ]+) items!$"), 54) {
        override fun getSimulator() = SelectAllSim()
        override fun getGUI() = SelectAllGui
    },
    MELODY("Click the button on time!", Regex("^Click the button on time!$"), 54) {
        override fun getSimulator() = MelodySim
        override fun getGUI() = MelodyGui
    };

    fun openHandler(guiName: String): TerminalHandler? {
        return when (this) {
            PANES -> PanesHandler()
            RUBIX -> RubixHandler()
            NUMBERS -> NumbersHandler()
            STARTS_WITH -> StartsWithHandler(regex.find(guiName)?.groupValues?.get(1) ?: run {
                modMessage("Failed to find letter, please report this!")
                return null
            })
            SELECT -> {
                SelectAllHandler(DyeColor.entries.find {
                    it.name.replace("_", " ")
                        .equals(regex.find(guiName)?.groupValues?.get(1)?.replace("SILVER", "LIGHT GRAY"), true)
                } ?: run {
                    modMessage("Failed to find letter, please report this!")
                    return null
                })
            }
            MELODY -> MelodyHandler()
        }
    }
}

private interface Type {
    fun getSimulator(): TermSimGUI
    fun getGUI(): TermGui
}
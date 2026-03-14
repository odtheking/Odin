package com.odtheking.odin.utils.skyblock.dungeon.terminals

import com.github.stivais.commodore.parsers.CommandParsable
import com.odtheking.odin.features.impl.floor7.termGUI.MelodyGui
import com.odtheking.odin.features.impl.floor7.termGUI.TermGui
import com.odtheking.odin.features.impl.floor7.termGUI.simpleTermGui
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
        private val gui = simpleTermGui(rows = 3, cols = 5, startRow = 1, startCol = 2)
        override fun getGUI() = gui
    },
    RUBIX("Change all to same color!", Regex("^Change all to same color!$"), 45) {
        override fun getSimulator() = RubixSim
        private val gui = simpleTermGui(rows = 3, cols = 3, startRow = 1, startCol = 3)
        override fun getGUI() = gui
    },
    NUMBERS("Click in order!", Regex("^Click in order!$"), 36) {
        override fun getSimulator() = NumbersSim
        private val gui = simpleTermGui(rows = 2, cols = 7, startRow = 1, startCol = 1)
        override fun getGUI() = gui
    },
    STARTS_WITH("What starts with: \"*\"?", Regex("^What starts with: '(\\w)'\\?$"), 45) {
        override fun getSimulator() = StartsWithSim()
        private val gui = simpleTermGui(rows = 3, cols = 7, startRow = 1, startCol = 1)
        override fun getGUI() = gui
    },
    SELECT("Select all the \"*\" items!", Regex("^Select all the ([\\w ]+) items!$"), 54) {
        override fun getSimulator() = SelectAllSim()
        private val gui = simpleTermGui(rows = 4, cols = 7, startRow = 1, startCol = 1)
        override fun getGUI() = gui
    },
    MELODY("Click the button on time!", Regex("^Click the button on time!$"), 54) {
        override fun getSimulator() = MelodySim
        private val gui = MelodyGui
        override fun getGUI() = gui
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
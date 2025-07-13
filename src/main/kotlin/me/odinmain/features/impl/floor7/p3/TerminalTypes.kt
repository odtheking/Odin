package me.odinmain.features.impl.floor7.p3

import com.github.stivais.commodore.parsers.CommandParsable
import me.odinmain.features.impl.floor7.p3.termGUI.*
import me.odinmain.features.impl.floor7.p3.termsim.*

@CommandParsable
enum class TerminalTypes(
    val windowName: String,
    val windowSize: Int
) : Type {
    PANES("Correct all the panes!", 45) {
        override fun getSimulator() = PanesSim
        override fun getGUI(): TermGui = PanesGui
    },
    RUBIX("Change all to same color!", 45) {
        override fun getSimulator() = RubixSim
        override fun getGUI(): TermGui = RubixGui
    },
    NUMBERS("Click in order!", 36) {
        override fun getSimulator() = NumbersSim
        override fun getGUI(): TermGui = NumbersGui
    },
    STARTS_WITH("What starts with:", 45) {
        override fun getSimulator() = StartsWithSim()
        override fun getGUI(): TermGui = StartsWithGui
    },
    SELECT("Select all the", 54) {
        override fun getSimulator() = SelectAllSim()
        override fun getGUI(): TermGui = SelectAllGui
    },
    MELODY("Click the button on time!", 54) {
        override fun getSimulator() = MelodySim
        override fun getGUI(): TermGui = MelodyGui
    },
}

private interface Type {
    fun getSimulator(): TermSimGUI
    fun getGUI(): TermGui
}
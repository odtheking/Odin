package me.odinmain.features.impl.floor7.p3

import com.github.stivais.commodore.parsers.CommandParsable
import me.odinmain.features.impl.floor7.p3.termGUI.*
import me.odinmain.features.impl.floor7.p3.termsim.*

@CommandParsable
enum class TerminalTypes(
    val windowName: String,
    val windowSize: Int,
    val gui: TermGui?
) : Type {
    PANES("Correct all the panes!", 45, PanesGui) {
        override fun getSimulator() = PanesSim
    },
    RUBIX("Change all to same color!", 45, RubixGui) {
        override fun getSimulator() = RubixSim
    },
    NUMBERS("Click in order!", 36, NumbersGui) {
        override fun getSimulator() = NumbersSim
    },
    STARTS_WITH("What starts with:", 45, StartsWithGui) {
        override fun getSimulator() = StartsWithSim()
    },
    SELECT("Select all the", 54, SelectAllGui) {
        override fun getSimulator() = SelectAllSim()
    },
    MELODY("Click the button on time!", 54, MelodyGui) {
        override fun getSimulator() = MelodySim
    },
}

private interface Type {
    fun getSimulator(): TermSimGUI
}
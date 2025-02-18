package me.odinmain.features.impl.floor7.p3

import com.github.stivais.commodore.parsers.CommandParsable
import me.odinmain.features.impl.floor7.p3.termGUI.*
import me.odinmain.features.impl.floor7.p3.termsim.*

@CommandParsable
enum class TerminalTypes(
    val guiName: String,
    val size: Int,
    val gui: TermGui?
) : Type {
    PANES("Correct all the panes!", 45, PanesGui) {
        override fun getSimulator() = CorrectPanesSim
    },
    RUBIX("Change all to same color!", 45, RubixGui) {
        override fun getSimulator() = RubixSim
    },
    ORDER("Click in order!", 36, OrderGui) {
        override fun getSimulator() = ClickInOrderSim
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

    NONE("None", 0, null) {
        override fun getSimulator() = StartGUI
    }
}

private interface Type {
    fun getSimulator(): TermSimGUI
}
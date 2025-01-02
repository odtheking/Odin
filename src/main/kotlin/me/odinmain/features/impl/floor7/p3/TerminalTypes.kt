package me.odinmain.features.impl.floor7.p3

import me.odinmain.features.impl.floor7.p3.termGUI.*

enum class TerminalTypes(val guiName: String, val size: Int, val gui: TermGui?) {
    PANES("Correct all the panes!",     45, PanesGui),
    RUBIX("Change all to same color!",  45, RubixGui),
    ORDER("Click in order!",            36, OrderGui),
    STARTS_WITH("What starts with:",    45, StartsWithGui),
    SELECT("Select all the",            54, SelectAllGui),
    MELODY("Click the button on time!", 54, MelodyGui),
    NONE("None",                         0, null)
}
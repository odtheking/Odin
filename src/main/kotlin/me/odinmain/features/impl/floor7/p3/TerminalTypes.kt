package me.odinmain.features.impl.floor7.p3

enum class TerminalTypes(val guiName: String, val size: Int) {
    PANES("Correct all the panes!",     45),
    RUBIX("Change all to same color!",  45),
    ORDER("Click in order!",            36),
    STARTS_WITH("What starts with:",    45),
    SELECT("Select all the",            54),
    MELODY("Click the button on time!", 54),
    NONE("None",                         0)
}
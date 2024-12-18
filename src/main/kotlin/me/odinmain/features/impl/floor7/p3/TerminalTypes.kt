package me.odinmain.features.impl.floor7.p3

enum class TerminalTypes(val guiName: String, val size: Int) {
    MELODY("Click the button on time!", 54),
    RUBIX("Change all to same color!",  45),
    PANES("Correct all the panes!",     45),
    STARTS_WITH("What starts with:",    45),
    ORDER("Click in order!",            36),
    SELECT("Select all the",            54),
    NONE("None",                         0)
}
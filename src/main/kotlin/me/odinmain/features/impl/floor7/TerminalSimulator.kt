package me.odinmain.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.impl.floor7.p3.termsim.*
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.sendCommand

object TerminalSimulator : Module(
    name = "Terminal Simulator",
    description = "Simulates a floor 7 terminal from phase 3.",
    category = Category.FLOOR7
) {
    private val ping by NumberSetting("Ping", 0, 0, 500, description = "Ping of the terminal.")
    val openStart by BooleanSetting("Open Start", false, description = "Open the start menu after you finish a terminal.")

    val termSimPBs = PersonalBest("Termsim", 6)

    override fun onKeybind() {
        sendCommand("termsim $ping", clientSide = true)
    }

    override fun onEnable() {
        super.onEnable()
        toggle()
        sendCommand("termsim $ping", clientSide = true)
    }

    fun openRandomTerminal(ping: Long = 0L) {
        when (listOf(TerminalTypes.PANES, TerminalTypes.RUBIX, TerminalTypes.ORDER, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT).random()) {
            TerminalTypes.STARTS_WITH -> StartsWith().open(ping)
            TerminalTypes.PANES       -> CorrectPanes.open(ping)
            TerminalTypes.SELECT      -> SelectAll().open(ping)
            TerminalTypes.ORDER       -> InOrder.open(ping)
            TerminalTypes.MELODY      -> Melody.open(ping)
            TerminalTypes.RUBIX       -> Rubix.open(ping)
            TerminalTypes.NONE        -> {}
        }
    }
}
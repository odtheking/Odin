package me.odinmain.features.impl.floor7

import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.impl.floor7.p3.termsim.*
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.sendCommand

object TerminalSimulator : Module(
    name = "Terminal Simulator",
    desc = "Simulates a floor 7 terminal from phase 3."
) {
    private val ping by NumberSetting("Ping", 0, 0, 500, desc = "Ping of the terminal.")

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
        when (listOf(TerminalTypes.PANES, TerminalTypes.RUBIX, TerminalTypes.NUMBERS, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT).random()) {
            TerminalTypes.STARTS_WITH -> StartsWithSim().open(ping)
            TerminalTypes.PANES       -> PanesSim.open(ping)
            TerminalTypes.SELECT      -> SelectAllSim().open(ping)
            TerminalTypes.NUMBERS       -> NumbersSim.open(ping)
            TerminalTypes.MELODY      -> MelodySim.open(ping)
            TerminalTypes.RUBIX       -> RubixSim.open(ping)
        }
    }
}
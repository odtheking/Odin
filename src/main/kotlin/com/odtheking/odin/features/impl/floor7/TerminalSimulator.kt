package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.termsim.*
import com.odtheking.odin.utils.PersonalBest
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalTypes

object TerminalSimulator : Module(
    name = "Terminal Simulator",
    description = "Simulates a floor 7 terminal from phase 3."
) {
    private val ping by NumberSetting("Ping", 0, 0, 500, 50, desc = "Ping of the terminal.")

    val termSimPBs = PersonalBest(this, "TermsimPBs")

    override fun onKeybind() {
        sendCommand("termsim $ping")
    }

    override fun onEnable() {
        super.onEnable()
        toggle()
        sendCommand("termsim $ping")
    }

    fun openRandomTerminal(ping: Long = 0L) {
        when (listOf(TerminalTypes.PANES, TerminalTypes.RUBIX, TerminalTypes.NUMBERS, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT).random()) {
            TerminalTypes.STARTS_WITH -> StartsWithSim().open(ping)
            TerminalTypes.PANES       -> PanesSim.open(ping)
            TerminalTypes.SELECT      -> SelectAllSim().open(ping)
            TerminalTypes.NUMBERS      -> NumbersSim.open(ping)
            TerminalTypes.MELODY      -> MelodySim.open(ping)
            TerminalTypes.RUBIX       -> RubixSim.open(ping)
        }
    }
}
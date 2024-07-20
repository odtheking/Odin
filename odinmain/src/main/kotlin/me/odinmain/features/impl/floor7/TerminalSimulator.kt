package me.odinmain.features.impl.floor7

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.sendCommand

object TerminalSimulator : Module(
    name = "Terminal Simulator",
    description = "Simulates a floor 7 terminal from phase 3."
) {
    val sendOnlyPB by SelectorSetting("Send Message", "Always", arrayListOf("Always", "Only PB"), description = "Send a message when a terminal is completed")
    private val ping by NumberSetting("Ping", 0, 0, 500, description = "Ping of the terminal.")
    private val repetitiveTerminals by NumberSetting("Random Terminals", 1, 1, 100, description = "Amount of random terminals.")
    val openStart by BooleanSetting("Open Start", false, description = "Open the start menu after you finish a terminal.")

    val simPBs = PersonalBest("Termsim", 5)

    override fun onKeybind() {
        sendCommand(if (repetitiveTerminals == 1) "termsim $ping" else "termsim $ping $repetitiveTerminals", clientSide = true)
        this.toggle()
    }

    override fun onEnable() {
        sendCommand(if (repetitiveTerminals == 1) "termsim $ping" else "termsim $ping $repetitiveTerminals", clientSide = true)
        super.onEnable()
        toggle()
    }
}
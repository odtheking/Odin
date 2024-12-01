package me.odinmain.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.impl.floor7.p3.termsim.*
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.getRandom
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.sendCommand
import net.minecraft.item.EnumDyeColor

object TerminalSimulator : Module(
    name = "Terminal Simulator",
    description = "Simulates a floor 7 terminal from phase 3.",
    category = Category.FLOOR7
) {
    val sendMessage by BooleanSetting("Send Message", false, description = "Send a message when a terminal is completed.")
    private val ping by NumberSetting("Ping", 0, 0, 500, description = "Ping of the terminal.")
    private val repetitiveTerminals by NumberSetting("Random Terminals", 1, 1, 100, description = "Amount of random terminals.")
    val openStart by BooleanSetting("Open Start", false, description = "Open the start menu after you finish a terminal.")

    val termSimPBs = PersonalBest("Termsim", 6)

    override fun onKeybind() {
        sendCommand(if (repetitiveTerminals == 1) "termsim $ping" else "termsim $ping $repetitiveTerminals", clientSide = true)
        this.toggle()
    }

    override fun onEnable() {
        sendCommand(if (repetitiveTerminals == 1) "termsim $ping" else "termsim $ping $repetitiveTerminals", clientSide = true)
        super.onEnable()
        toggle()
    }

    fun openRandomTerminal(ping: Long = 0L, cons: Long = 0L) {
        when (listOf(TerminalTypes.PANES, TerminalTypes.RUBIX, TerminalTypes.ORDER, TerminalTypes.STARTS_WITH, TerminalTypes.SELECT).random()) {
            TerminalTypes.PANES       -> CorrectPanes.open(ping, cons)
            TerminalTypes.RUBIX       -> Rubix.open(ping, cons)
            TerminalTypes.ORDER       -> InOrder.open(ping, cons)
            TerminalTypes.STARTS_WITH -> StartsWith(StartsWith.letters.shuffled().first()).open(ping, cons)
            TerminalTypes.SELECT      -> SelectAll(EnumDyeColor.entries.getRandom().name.replace("_", " ").uppercase()).open(ping, cons)
            TerminalTypes.MELODY      -> Melody.open(ping, cons)
            TerminalTypes.NONE        -> {}
        }
    }
}
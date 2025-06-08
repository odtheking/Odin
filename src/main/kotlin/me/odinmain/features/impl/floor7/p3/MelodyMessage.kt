package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGUI
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.skyblock.sendCommand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MelodyMessage : Module(
    name = "Melody Message",
    desc = "Helpful messages for the melody terminal in floor 7."
) {
    private val sendMelodyMessage by BooleanSetting("Send Melody Message", true, desc = "Sends a message when the melody terminal opens.")
    private val melodyMessage by StringSetting("Melody Message", "Melody Terminal start!", 128, desc = "Message sent when the melody terminal opens.").withDependency { sendMelodyMessage }
    private val melodyProgress by BooleanSetting("Melody Progress", false, desc = "Tells the party about melody terminal progress.")
    private val melodySendCoords by BooleanSetting("Melody Send Coords", false, desc = "Sends the coordinates of the melody terminal.").withDependency { melodyProgress }

    private var claySlots = hashMapOf(25 to "Melody 25%", 34 to "Melody 50%", 43 to "Melody 75%")

    @SubscribeEvent
    fun onTermLoad(event: TerminalEvent.Opened) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || event.terminal.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI) return
        if (sendMelodyMessage) partyMessage(melodyMessage)
        if (melodySendCoords) sendCommand("od sendcoords", true)

        claySlots = hashMapOf(25 to "Melody 25%", 34 to "Melody 50%", 43 to "Melody 75%")
    }

    init {
        execute(250) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || TerminalSolver.currentTerm?.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI || !melodyProgress) return@execute

            val greenClayIndices = claySlots.keys.filter { index -> TerminalSolver.currentTerm?.items?.get(index)?.metadata == 5 }.ifEmpty { return@execute }

            partyMessage(claySlots[greenClayIndices.last()] ?: return@execute)
            greenClayIndices.forEach { claySlots.remove(it) }
        }
    }
}
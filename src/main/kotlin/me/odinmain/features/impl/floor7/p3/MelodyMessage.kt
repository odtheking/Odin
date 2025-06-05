package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGUI
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.skyblock.sendCommand
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MelodyMessage : Module(
    name = "Melody Message",
    desc = "Helpful messages for the melody terminal in floor 7."
) {
    private val sendMelodyMessage by BooleanSetting("Send Melody Message", true, desc = "Sends a message when the melody terminal opens.")
    private val melodyMessage by StringSetting("Melody Message", "Melody Terminal start!", 128, desc = "Message sent when the melody terminal opens.").withDependency { sendMelodyMessage }
    private val melodyProgress by BooleanSetting("Melody Progress", false, desc = "Tells the party about melody terminal progress.")
    private val melodySendCoords by BooleanSetting("Melody Send Coords", false, desc = "Sends the coordinates of the melody terminal.").withDependency { melodyProgress }

    private var claySlots = hashMapOf(25 to "25%", 34 to "50%", 43 to "75%")

    @SubscribeEvent
    fun onGuiLoad(event: TerminalEvent.Opened) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || event.terminal.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI) return
        if (sendMelodyMessage) partyMessage(melodyMessage)
        if (melodySendCoords) sendCommand("od sendcoords", true)

        claySlots = hashMapOf(25 to "25%", 34 to "50%", 43 to "75%")
    }

    init {
        execute(250) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3 || TerminalSolver.currentTerm?.type != TerminalTypes.MELODY || mc.currentScreen is TermSimGUI) return@execute

            val containerChest = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (containerChest.name != "Click the button on time!" || !melodyProgress) return@execute

            val greenClayIndices = claySlots.keys.filter { index -> containerChest.getSlot(index)?.stack?.metadata == 5 }.ifEmpty { return@execute }
            val lastSlot = greenClayIndices.last()
            val progress = claySlots[lastSlot] ?: return@execute

            partyMessage("$melodyMessage $progress")
            greenClayIndices.forEach { claySlots.remove(it) }
        }
    }
}
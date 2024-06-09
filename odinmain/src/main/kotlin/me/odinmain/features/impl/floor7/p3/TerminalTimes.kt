package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.TerminalClosedEvent
import me.odinmain.events.impl.TerminalOpenedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalTimes : Module(
    name = "Terminal Times",
    description = "Records the time taken to complete terminals in floor 7.",
    category = Category.FLOOR7
) {
    private val sendMessage: Boolean by DualSetting("Send Message", "Always", "Only PB", true, description = "Send a message when a terminal is completed")
    private val reset: () -> Unit by ActionSetting("Reset pbs") {
        repeat(6) { i -> termPBs.set(i, 999.0) }
        modMessage("§6Terminal PBs §fhave been reset.")
    }

    private val termPBs = PersonalBest("Terminals", 6)
    private var startTimer = 0L

    @SubscribeEvent
    fun onTerminalOpen(event: TerminalOpenedEvent) {
        startTimer = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onTerminalClose(event: TerminalClosedEvent) {
        val time = (System.currentTimeMillis() - startTimer) / 1000.0

        termPBs.time(event.type.ordinal, time, "s§7!", "§a$name §7solved in §6", addPBString = true, addOldPBString = true, sendMessage)
    }
}
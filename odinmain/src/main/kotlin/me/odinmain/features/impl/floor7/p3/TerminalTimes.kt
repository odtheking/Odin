package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.RealServerTick
import me.odinmain.events.impl.TerminalOpenedEvent
import me.odinmain.events.impl.TerminalSolvedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGui
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

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

    private val terminalSplits: Boolean by BooleanSetting("Terminal Splits", default = true, description = "Adds the time when a term was completed to its message, and sends the total term time after terms are done.")

    private val termPBs = PersonalBest("Terminals", 6)
    private var startTimer = 0L
    private var type = TerminalTypes.NONE

    @SubscribeEvent
    fun onTerminalOpen(event: TerminalOpenedEvent) {
        if (event.type == type || mc.currentScreen is TermSimGui) return
        type = event.type
        startTimer = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onTerminalClose(event: TerminalSolvedEvent) {
        if (type == TerminalTypes.NONE || mc.currentScreen is TermSimGui || event.playerName != mc.thePlayer?.name) return
        termPBs.time(event.type.ordinal, (System.currentTimeMillis() - startTimer) / 1000.0, "s§7!", "§a${event.type.guiName} §7solved in §6", addPBString = true, addOldPBString = true, sendOnlyPB = sendMessage)
        type = TerminalTypes.NONE
    }

    private val terminalCompleteRegex = Regex("(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)")

    private var gateBlown = false
    private var completed: Pair<Int, Int> = Pair(0, 7)
    private var phaseTimer = 0L
    private var sectionTimer = 0L
    private val times = mutableListOf<Long>()

    @SubscribeEvent
    fun onMessage(event: ClientChatReceivedEvent) {
        if (event.message.unformattedText.noControlCodes.matches(terminalCompleteRegex) && terminalSplits) event.isCanceled = true
    }

    init {
        onMessage("The gate has been destroyed!", false, { enabled && terminalSplits }) {
            if (completed.first == completed.second) resetSection()
            else gateBlown = true
        }

        onMessage("[BOSS] Goldor: Who dares trespass into my domain?", false, { enabled && terminalSplits }) {
            resetSection(true)
        }

        onMessage(terminalCompleteRegex, { enabled && terminalSplits }) {
            val (name, activated, type, current, total) = terminalCompleteRegex.find(it)?.destructured ?: return@onMessage
            modMessage("§6$name §a$activated a $type! (§c${current}§a/${total}) §8(§7${sectionTimer.seconds}s §8| §7${phaseTimer.seconds}s§8)", false)
            if ((current == total && gateBlown) || (current.toIntOrNull() ?: return@onMessage) < completed.first) resetSection()
            else completed = Pair(current.toIntOrNull() ?: return@onMessage, total.toIntOrNull() ?: return@onMessage)
        }

        onMessage("The Core entrance is opening!", false, { enabled && terminalSplits }) {
            resetSection()
            modMessage("§bTimes: §a${times.joinToString(" §8| ") { "§a${it.seconds}s" }}§8, §bTotal: §ag${phaseTimer.seconds}s")
        }
    }

    private val Long.seconds get() = this.toFloat() / 20

    private fun resetSection(full: Boolean = false) {
        if (full) {
            times.clear()
            phaseTimer = 0L
        } else times.add(sectionTimer)
        completed = Pair(0, 7)
        sectionTimer = 0L
        gateBlown = false
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        if (!terminalSplits) return
        phaseTimer++
        sectionTimer++
    }
}
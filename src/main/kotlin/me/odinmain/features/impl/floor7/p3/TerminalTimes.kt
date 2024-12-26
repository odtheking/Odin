package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.events.impl.TerminalEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.TerminalSimulator
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGui
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalTimes : Module(
    name = "Terminal Times",
    description = "Records the time taken to complete terminals in floor 7.",
    category = Category.FLOOR7
) {
    private val sendMessage by BooleanSetting("Send Message", false, description = "Send a message when a terminal is completed.")
    private val reset by ActionSetting("Reset pbs", description = "Resets the terminal PBs.") {
        repeat(6) { i -> terminalPBs.set(i, 999.0) }
        modMessage("§6Terminal PBs §fhave been reset.")
    }

    private val terminalSplits by BooleanSetting("Terminal Splits", default = true, description = "Adds the time when a term was completed to its message, and sends the total term time after terms are done.")
    private val useRealTime by BooleanSetting("Use Real Time", default = true, description = "Use real time rather than server ticks.")

    private val terminalPBs = PersonalBest("Terminals", 7)

    @SubscribeEvent
    fun onTerminalClose(event: TerminalEvent.Solved) {
        if (event.terminal.type == TerminalTypes.NONE) return
        val pbs = if (mc.currentScreen is TermSimGui) TerminalSimulator.termSimPBs else terminalPBs
        pbs.time(event.terminal.type.ordinal, (System.currentTimeMillis() - event.terminal.timeOpened) / 1000.0, "s§7!", "§a${event.terminal.guiName}${if (mc.currentScreen is TermSimGui) " §7(termsim)" else ""} §7solved in §6", addPBString = true, addOldPBString = true, sendOnlyPB = sendMessage)
    }

    private val terminalCompleteRegex = Regex("(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)")

    private var completed: Pair<Int, Int> = Pair(0, 7)
    private val times = mutableListOf<Double>()
    private var gateBlown = false
    private var sectionTimer = 0L
    private var currentTick = 0L
    private var phaseTimer = 0L

    @SubscribeEvent
    fun onMessage(event: ClientChatReceivedEvent) {
        if (event.message.unformattedText.noControlCodes.matches(terminalCompleteRegex) && terminalSplits) event.isCanceled = true
    }

    init {
        onMessage(Regex("The gate has been destroyed!"), { enabled && terminalSplits }) {
            if (completed.first == completed.second) resetSection() else gateBlown = true
        }

        onMessage(Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?"), { enabled && terminalSplits }) {
            resetSection(true)
        }

        onMessage(terminalCompleteRegex, { enabled && terminalSplits }) {
            val (name, activated, type, current, total) = terminalCompleteRegex.find(it)?.destructured ?: return@onMessage
            modMessage("§6$name §a$activated a $type! (§c${current}§a/${total}) §8(§7${sectionTimer.seconds}s §8| §7${phaseTimer.seconds}s§8)", "")
            if ((current == total && gateBlown) || (current.toIntOrNull() ?: return@onMessage) < completed.first) resetSection()
            else completed = Pair(current.toIntOrNull() ?: return@onMessage, total.toIntOrNull() ?: return@onMessage)
        }

        onMessage(Regex("The Core entrance is opening!"), { enabled && terminalSplits }) {
            resetSection()
            modMessage("§bTimes: §a${times.joinToString(" §8| ") { "§a${it}s" }}§8, §bTotal: §a${phaseTimer.seconds}s")
        }

        onWorldLoad {
            resetSection(true)
        }
    }

    private val Long.seconds
        get() = ((if (useRealTime) System.currentTimeMillis() else currentTick) - this) / 1000.0

    private fun resetSection(full: Boolean = false) {
        if (full) {
            times.clear()
            phaseTimer = if (useRealTime) System.currentTimeMillis() else currentTick
        } else times.add(sectionTimer.seconds)
        completed = Pair(0, 7)
        sectionTimer = if (useRealTime) System.currentTimeMillis() else currentTick
        gateBlown = false
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (!terminalSplits || useRealTime) return
        currentTick += 50
    }
}
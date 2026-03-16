package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TerminalEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.floor7.termsim.TermSimGUI
import com.odtheking.odin.utils.ChatManager.hideMessage
import com.odtheking.odin.utils.PersonalBest
import com.odtheking.odin.utils.modMessage

object TerminalTimes : Module(
    name = "Terminal Times",
    description = "Records the time taken to complete terminals in floor 7."
) {
    private val sendMessage by BooleanSetting("Send only PB", false, desc = "Send a message when a terminal is completed only when it is PB.")
    private val reset by ActionSetting("Reset pbs", desc = "Resets the terminal PBs.") {
        terminalPBs.reset()
        modMessage("§6Terminal PBs §fhave been reset.")
    }

    private val terminalSplits by BooleanSetting("Terminal Splits", true, desc = "Adds the time when a term was completed to its message, and sends the total term time after terms are done.")
    private val useRealTime by BooleanSetting("Use Real Time", true, desc = "Use real time rather than server ticks.")

    private val terminalCompleteRegex = Regex("^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)$")
    private val gateDestroyedRegex = Regex("The gate has been destroyed!")
    private val goldorRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")
    private val coreOpeningRegex = Regex("^The Core entrance is opening!$")

    private val terminalPBs = PersonalBest(this, "TerminalPBs")
    private var completed: Pair<Int, Int> = Pair(0, 7)
    private val times = mutableListOf<Float>()
    private var gateBlown = false
    private var sectionTimer = 0L
    private var currentTick = 0L
    private var phaseTimer = 0L

    init {
        on<TerminalEvent.Solve> {
            val pbs = if (mc.screen is TermSimGUI) TerminalSimulator.termSimPBs else terminalPBs
            pbs.time(terminal.type.name, (System.currentTimeMillis() - terminal.timeOpened) / 1000f, "s§7!", "§a${terminal.type.termName}${if (mc.screen is TermSimGUI) " §7(termsim)" else ""} §7solved in §6", sendOnlyPB = sendMessage)
        }

        on<ChatPacketEvent> {
            if (!terminalSplits) return@on
            terminalCompleteRegex.find(value)?.destructured?.let { (name, activated, type, current, total) ->
                hideMessage()

                modMessage("§6$name §a$activated a $type! (§c${current}§a/${total}) §8(§7${sectionTimer.seconds}s §8| §7${phaseTimer.seconds}s§8)", "")

                if ((current == total && gateBlown) || (current.toIntOrNull() ?: return@on) < completed.first) resetSection()
                else completed = Pair(current.toIntOrNull() ?: return@on, total.toIntOrNull() ?: return@on)
                return@on
            }

            when {
                gateDestroyedRegex.matches(value) -> if (completed.first == completed.second) resetSection() else gateBlown = true

                goldorRegex.matches(value) -> resetSection(true)

                coreOpeningRegex.matches(value) -> {
                    resetSection()
                    modMessage("§bTimes: §a${times.joinToString(" §8| ") { "§a${it}s" }}§8, §bTotal: §a${phaseTimer.seconds}s")
                }
            }
        }

        on<TickEvent.Server> {
            if (terminalSplits && !useRealTime) currentTick += 50
        }

        on<WorldEvent.Load> {
            resetSection(true)
        }
    }

    private inline val Long.seconds
        get() = ((if (useRealTime) System.currentTimeMillis() else currentTick) - this) / 1000f

    private fun resetSection(full: Boolean = false) {
        if (full) {
            times.clear()
            phaseTimer = if (useRealTime) System.currentTimeMillis() else currentTick
        } else times.add(sectionTimer.seconds)
        completed = Pair(0, 7)
        sectionTimer = if (useRealTime) System.currentTimeMillis() else currentTick
        gateBlown = false
    }
}
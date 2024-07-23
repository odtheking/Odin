package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.TerminalOpenedEvent
import me.odinmain.events.impl.TerminalSolvedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termsim.TermSimGui
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.modMessage
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
    private val times = mutableListOf<Int>()

    init {
        onMessage("The gate has been destroyed!", false) {
            if (completed.first == completed.second) resetSection()
            else gateBlown = true
        }

        onMessage("[BOSS] Goldor: Who dares trespass into my domain?", false) {
            resetSection(true)
        }

        onMessageCancellable(terminalCompleteRegex) {
            val matchResult = terminalCompleteRegex.find(it.message)?.groups ?: return@onMessageCancellable
            val complete = Pair(matchResult[4]?.value?.toIntOrNull() ?: return@onMessageCancellable, matchResult[5]?.value?.toIntOrNull() ?: return@onMessageCancellable)
            modMessage("§6${matchResult[1]?.value} §a${matchResult[2]?.value} a ${matchResult[3]?.value}! (§c${complete.first}§a/${complete.second}) §8(§7${sectionTimer} §8| §7${phaseTimer}§8)", false)
            if ((complete.first == complete.second && gateBlown) || complete.first < completed.first) resetSection() else completed = complete
            it.isCanceled = true
        }

        onMessage("The Core entrance is opening!", false) {
            resetSection()
            modMessage("§bTimes: §a${times.joinToString(" §8| §a")}§8, §bTotal: $phaseTimer")
        }
    }

    private fun resetSection(full: Boolean = false) {
        if (full) {
            times.clear()
            phaseTimer = 0L
        } else times.add(sectionTimer.toInt())
        completed = Pair(0, 7)
        sectionTimer = 0L
        gateBlown = false
    }

    @SubscribeEvent
    fun onServerTick(event: ClientTickEvent) {
        phaseTimer++
        sectionTimer++
    }
}
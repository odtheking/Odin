package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.ServerTick
import com.odtheking.odin.utils.toFixed

object TickTimers : Module(
    name = "Tick Timers",
    description = "Displays timers for Necron, Goldor, Storm, Outbounds, Secrets and time lost to server lag."
) {
    private val displayInTicks by BooleanSetting("Display in Ticks", false, desc = "Display the timers in ticks instead of seconds.")
    private val symbolDisplay by BooleanSetting("Display Symbol", true, desc = "Displays s or t after the timers.")
    private val showPrefix by BooleanSetting("Show Prefix", true, desc = "Shows the prefix of the timers.")

    private val goldorRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")
    private val coreOpeningRegex = Regex("^The Core entrance is opening!$")
    private val stormEndRegex = Regex("^\\[BOSS] Storm: I should have known that I stood no chance\\.$")
    private val stormStartRegex = Regex("^\\[BOSS] Storm: Pathetic Maxor, just like expected\\.$")
    private val stormPyRegex = Regex("^\\[BOSS] Storm: (ENERGY HEED MY CALL|THUNDER LET ME BE YOUR CATALYST)!$")
    private val professorFireFreezeRegex = Regex("^\\[BOSS] The Professor: Oh\\? You found my Guardians' one weakness\\?$")

    // Every timer stores the server tick it started or ends on (-1 = inactive); the old countdown values are derived from it.
    private fun left(endTick: Long) = if (endTick < 0) -1 else (endTick - ServerTick.now).toInt()
    private fun cycle(startTick: Long, period: Int) = if (startTick < 0) -1 else (startTick - ServerTick.now).mod(period)

    private var goldorTickStart = -1L
    private var goldorStartEnd = -1L
    private val goldorTickTime get() = cycle(goldorTickStart, 60)
    private val goldorStartTime get() = left(goldorStartEnd)

    private val goldorHud: HudElement by TextHUD("Goldor Hud", "Displays a timer for Goldor's Core entrance opening.", example = { formatTimer(35, 60, "§7Tick:") }) {
        when {
            goldorStartTime >= 0 && startTimer -> formatTimer(goldorStartTime, 64, "§aStart:")
            goldorTickTime >= 0 -> formatTimer(goldorTickTime, 60, "§7Tick:")
            else -> null
        }
    }
    private val startTimer by BooleanSetting("Start timer", false, desc = "Displays a timer counting down until devices/terms are able to be activated/completed.").withDependency { goldorHud.enabled }

    private var padStart = -1L
    private val padTickTime get() = cycle(padStart, 20)

    private val stormHud by TextHUD("Storm Pad Hud", "Displays a timer for Storm's Pad.", example = { formatTimer(15, 20, "§bPad:") }) {
        if (padTickTime >= 0) formatTimer(padTickTime, 20, "§bPad:") else null
    }

    private var lightningEnd = -1L
    private val lightningTickTime get() = left(lightningEnd)

    private val lightningHud by TextHUD("Storm Lightning Hud", "Displays a timer for Storm's Lightning.", example = { formatTimer(520, 520, "§bLightning:") }) {
        if (lightningTickTime >= 0) formatTimer(lightningTickTime, 520, "§bLightning:") else null
    }

    private var pyTriggered = false
    private var pyEnd = -1L
    private val pyTickTime get() = left(pyEnd)

    private val pyHud by TextHUD("Storm PY Hud", "Displays a timer for when to crush storm under the purple pillar.", example = { formatTimer(55, 55, "§bPY:") }) {
        if (pyTickTime >= 0) formatTimer(pyTickTime, 55, "§bPY:") else null
    }

    private var stormStart = -1L
    private val stormTick get() = if (stormStart < 0) -1 else (ServerTick.now - stormStart).toInt()

    private val stormTickHud by TextHUD("Storm Tick Hud", "Displays a timer for Storm's second phase, optionally counting down to the crush window.", example = { formatTimer(200, 540, "§bStorm:", "§a") }) {
        if (stormTick >= 0) formatTimer(stormTick, 540, "§bStorm:") else null
    }

    private var fireFreezeEnd = -1L
    private val fireFreezeTime get() = left(fireFreezeEnd)

    private val fireFreezeHud by TextHUD("Fire Freeze Hud", "Displays a timer for when to use fire freeze in M3.", example = { formatTimer(50, 60, "Fire Freeze:") }) {
        val timeTillFreeze = fireFreezeTime - 100
        when {
            timeTillFreeze > 0 -> formatTimer(timeTillFreeze, 60, "Fire Freeze:")
            fireFreezeTime >= 0 -> "Use Fire Freeze"
            else -> null
        }
    }

    // Real time minus server time (ticks * 50ms) since the first tick in this world, sampled as each tick arrives.
    private var lagStartTick = -1L
    private var lagStartMs = 0L
    @Volatile private var lagMs = 0L

    private val serverLagHud by TextHUD("Server Lag Hud", "Displays how much time has been lost to server lag since joining the world.", example = { formatLag(1250) }) {
        if (lagStartTick >= 0) formatLag(lagMs) else null
    }

    init {
        on<TickEvent.Server> {
            val nowMs = System.currentTimeMillis()
            if (lagStartTick < 0) {
                lagStartTick = ServerTick.now
                lagStartMs = nowMs
            }
            lagMs = (nowMs - lagStartMs - (ServerTick.now - lagStartTick) * 50).coerceAtLeast(0)
        }

        on<MessageEvent.Chat> {
            when {
                message.matches(goldorRegex) -> goldorTickStart = ServerTick.now
                message.matches(coreOpeningRegex) -> {
                    goldorStartEnd = -1
                    goldorTickStart = -1
                }
                message.matches(stormEndRegex) -> {
                    goldorStartEnd = ServerTick.now + 64
                    padStart = -1
                    stormStart = -1
                }
                message.matches(stormStartRegex) -> {
                    padStart = ServerTick.now
                    lightningEnd = ServerTick.now + 520
                    stormStart = ServerTick.now
                }
                !pyTriggered && message.matches(stormPyRegex) -> {
                    pyTriggered = true
                    pyEnd = ServerTick.now + 55
                }
                message.matches(professorFireFreezeRegex) -> fireFreezeEnd = ServerTick.now + 160
            }
        }

        on<LevelEvent.Load> {
            goldorStartEnd = -1
            goldorTickStart = -1
            padStart = -1
            lightningEnd = -1
            pyEnd = -1
            pyTriggered = false
            stormStart = -1
            fireFreezeEnd = -1
            lagStartTick = -1
            lagMs = 0
        }
    }

    private fun formatLag(ms: Long): String {
        val color = when {
            ms < 1000 -> "§a"
            ms < 3000 -> "§6"
            else -> "§c"
        }
        val lagDisplay = if (displayInTicks) "${ms / 50}${if (symbolDisplay) "t" else ""}" else "${(ms / 1000f).toFixed()}${if (symbolDisplay) "s" else ""}"
        return "${if (showPrefix) "§7Lag: " else ""}$color$lagDisplay"
    }

    private fun formatTimer(time: Int, max: Int, prefix: String, overrideColor: String? = null): String {
        val color = overrideColor ?: when {
            time.toFloat() >= max * 0.66 -> "§a"
            time.toFloat() >= max * 0.33 -> "§6"
            else -> "§c"
        }
        val timeDisplay = if (displayInTicks) "$time${if (symbolDisplay) "t" else ""}" else "${(time / 20f).toFixed()}${if (symbolDisplay) "s" else ""}"
        return "${if (showPrefix) "$prefix " else ""}$color$timeDisplay"
    }
}

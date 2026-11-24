package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed

object TickTimers : Module(
    name = "Tick Timers",
    description = "Displays timers for Necron, Goldor, Storm, Outbounds and Secrets."
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

    private var goldorTickTime = -1
    private var goldorStartTime = -1

    private val goldorHud: HudElement by TextHUD("Goldor Hud", "Displays a timer for Goldor's Core entrance opening.", example = { formatTimer(35, 60, "§7Tick:") }) {
        when {
            goldorStartTime >= 0 && startTimer -> formatTimer(goldorStartTime, 64, "§aStart:")
            goldorTickTime >= 0 -> formatTimer(goldorTickTime, 60, "§7Tick:")
            else -> null
        }
    }
    private val startTimer by BooleanSetting("Start timer", false, desc = "Displays a timer counting down until devices/terms are able to be activated/completed.").withDependency { goldorHud.enabled }

    private var padTickTime = -1

    private val stormHud by TextHUD("Storm Pad Hud", "Displays a timer for Storm's Pad.", example = { formatTimer(15, 20, "§bPad:") }) {
        if (padTickTime >= 0) formatTimer(padTickTime, 20, "§bPad:") else null
    }

    private var lightningTickTime = -1

    private val lightningHud by TextHUD("Storm Lightning Hud", "Displays a timer for Storm's Lightning.", example = { formatTimer(520, 520, "§bLightning:") }) {
        if (lightningTickTime >= 0) formatTimer(lightningTickTime, 520, "§bLightning:") else null
    }

    private var pyTriggered = false
    private var pyTickTime = -1

    private val pyHud by TextHUD("Storm PY Hud", "Displays a timer for when to crush storm under the purple pillar.", example = { formatTimer(55, 55, "§bPY:") }) {
        if (pyTickTime >= 0) formatTimer(pyTickTime, 55, "§bPY:") else null
    }

    private var stormTick = -1

    private val stormTickHud by TextHUD("Storm Tick Hud", "Displays a timer for Storm's second phase, optionally counting down to the crush window.", example = { formatTimer(200, 540, "§bStorm:", "§a") }) {
        if (stormTick >= 0) formatTimer(stormTick, 540, "§bStorm:") else null
    }

    private var fireFreezeTime = -1

    private val fireFreezeHud by TextHUD("Fire Freeze Hud", "Displays a timer for when to use fire freeze in M3.", example = { formatTimer(50, 60, "Fire Freeze:") }) {
        val timeTillFreeze = fireFreezeTime - 100
        when {
            timeTillFreeze > 0 -> formatTimer(timeTillFreeze, 60, "Fire Freeze:")
            fireFreezeTime >= 0 -> "Use Fire Freeze"
            else -> null
        }
    }

    init {
        on<MessageEvent.Chat> {
            when {
                message.matches(goldorRegex) -> goldorTickTime = 60
                message.matches(coreOpeningRegex) -> {
                    goldorStartTime = -1
                    goldorTickTime = -1
                }
                message.matches(stormEndRegex) -> {
                    goldorStartTime = 64
                    padTickTime = -1
                    stormTick = -1
                }
                message.matches(stormStartRegex) -> {
                    padTickTime = 20
                    lightningTickTime = 520
                    stormTick = 0
                }
                !pyTriggered && message.matches(stormPyRegex) -> {
                    pyTriggered = true
                    pyTickTime = 55
                }
                message.matches(professorFireFreezeRegex) -> fireFreezeTime = 160
            }
        }

        on<TickEvent.Server> {
            if (!DungeonUtils.inBoss) return@on
            if (goldorTickTime == 0 && goldorStartTime <= 0 && goldorHud.enabled) goldorTickTime = 60
            if (goldorStartTime >= 0) goldorStartTime--
            if (goldorTickTime >= 0) goldorTickTime--
            if (padTickTime == 0 && stormHud.enabled) padTickTime = 20
            if (padTickTime >= 0) padTickTime--
            if (lightningTickTime >= 0) lightningTickTime--
            if (pyTickTime >= 0) pyTickTime--
            if (stormTick >= 0) stormTick++
            if (fireFreezeTime >= 0) fireFreezeTime--
        }

        on<LevelEvent.Load> {
            goldorStartTime = -1
            goldorTickTime = -1
            padTickTime = -1
            lightningTickTime = -1
            pyTickTime = -1
            pyTriggered = false
            stormTick = -1
            fireFreezeTime = -1
        }
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

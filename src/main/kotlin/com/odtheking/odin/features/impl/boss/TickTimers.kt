package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.events.ChatMessageEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed

object TickTimers : Module(
    name = "Tick Timers",
    description = "Displays timers for Necron, Goldor, Storm, Outbounds and Secrets."
) {
    private val displayInTicks by BooleanSetting("Display in Ticks", false, desc = "Display the timers in ticks instead of seconds.")
    private val symbolDisplay by BooleanSetting("Display Symbol", true, desc = "Displays s or t after the timers.")
    private val showPrefix by BooleanSetting("Show Prefix", true, desc = "Shows the prefix of the timers.")

    private val necronRegex = Regex("^\\[BOSS] Necron: I'm afraid, your journey ends now\\.$")
    private val goldorRegex = Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$")
    private val coreOpeningRegex = Regex("^The Core entrance is opening!$")
    private val stormEndRegex = Regex("^\\[BOSS] Storm: I should have known that I stood no chance\\.$")
    private val stormStartRegex = Regex("^\\[BOSS] Storm: Pathetic Maxor, just like expected\\.$")
    private val stormPyRegex = Regex("^\\[BOSS] Storm: (ENERGY HEED MY CALL|THUNDER LET ME BE YOUR CATALYST)!$")

    private var necronTime = -1

    private val necronHud by HUD("Necron Hud", "Displays a timer for Necron's drop.") {
        if (it)                   textDim(formatTimer(35, 60, "§4Necron dropping in"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else if (necronTime >= 0) textDim(formatTimer(necronTime, 60, "§4Necron dropping in"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else 0 to 0
    }

    private var goldorTickTime = -1
    private var goldorStartTime = -1

    private val goldorHud: HudElement by HUD("Goldor Hud", "Displays a timer for Goldor's Core entrance opening.") {
        if (it) textDim(formatTimer(35, 60, "§7Tick:"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else if ((goldorStartTime >= 0 && startTimer) || goldorTickTime >= 0) {
            val (prefix, time, max) = if (goldorStartTime >= 0 && startTimer) Triple("§aStart:", goldorStartTime, 100) else Triple("§7Tick:", goldorTickTime, 60)
            textDim(formatTimer(time, max, prefix), 0, 0, Colors.MINECRAFT_DARK_RED)
        } else 0 to 0
    }
    private val startTimer by BooleanSetting("Start timer", false, desc = "Displays a timer counting down until devices/terms are able to be activated/completed.").withDependency { goldorHud.enabled }

    private var padTickTime = -1

    private val stormHud by HUD("Storm Pad Hud", "Displays a timer for Storm's Pad.") {
        if (it)                    textDim(formatTimer(15, 20, "§bPad:"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else if (padTickTime >= 0) textDim(formatTimer(padTickTime, 20, "§bPad:"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else 0 to 0
    }

    private var lightningTickTime = -1

    private val lightningHud by HUD("Storm Lightning Hud", "Displays a timer for Storm's Lightning.") {
        if (it)                          textDim(formatTimer(560, 560, "§bLightning:"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else if (lightningTickTime >= 0) textDim(formatTimer(lightningTickTime, 560, "§bLightning:"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else 0 to 0
    }

    private var pyTriggered = false
    private var pyTickTime = -1

    private val pyHud by HUD("Storm PY Hud", "Displays a timer for when to crush storm under the purple pillar.") {
        if (it)                   textDim(formatTimer(95, 95, "§bPY:"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else if (pyTickTime >= 0) textDim(formatTimer(pyTickTime, 95, "§bPY:"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else 0 to 0
    }

    private var stormTick = -1

    private val stormTickHud by HUD("Storm Tick Hud", "Displays a timer for Storm's second phase, optionally counting down to the crush window.") {
        if (it) {
            val (time, max, prefix) = Triple(200, 620, "§bStorm:")
            textDim(formatTimer(time, max, prefix), 0, 0, Colors.MINECRAFT_DARK_RED)
        } else if (stormTick >= 0) textDim(formatTimer(stormTick, 620, "§bStorm:"), 0, 0, Colors.MINECRAFT_DARK_RED)
        else 0 to 0
    }

    init {
        on<ChatMessageEvent> {
            when {
                value.matches(necronRegex) -> necronTime = 60
                value.matches(goldorRegex) -> goldorTickTime = 60
                value.matches(coreOpeningRegex) -> {
                    goldorStartTime = -1
                    goldorTickTime = -1
                }
                value.matches(stormEndRegex) -> {
                    goldorStartTime = 104
                    padTickTime = -1
                    stormTick = -1
                }
                value.matches(stormStartRegex) -> {
                    padTickTime = 20
                    lightningTickTime = 560
                    stormTick = 0
                }
                !pyTriggered && value.matches(stormPyRegex) -> {
                    pyTriggered = true
                    pyTickTime = 95
                }
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
            if (necronTime >= 0) necronTime--
            if (stormTick >= 0) stormTick++
        }

        on<LevelEvent.Load> {
            goldorStartTime = -1
            goldorTickTime = -1
            padTickTime = -1
            lightningTickTime = -1
            pyTickTime = -1
            pyTriggered = false
            necronTime = -1
            stormTick = -1
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
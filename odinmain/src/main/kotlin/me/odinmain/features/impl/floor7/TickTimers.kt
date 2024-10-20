package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.mcTextAndWidth
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Locale

object TickTimers : Module(
    name = "Tick Timers",
    category = Category.FLOOR7,
    description = "Various tick timers for the floor 7 boss."
) {
    private val displayInTicks: Boolean by DualSetting("Timer Style", "Seconds", "Ticks", default = false, description = "Which Style to display the timers in.")
    private val symbolDisplay: Boolean by BooleanSetting("Display Symbol", default = true, description = "Displays s or t after the timers.")
    private val showPrefix: Boolean by BooleanSetting("Show Prefix", default = true, description = "Shows the prefix of the timers.")

    private val necronHud by HudSetting("Necron Hud", 10f, 10f, 1f, true) {
        if (it) {
            mcTextAndWidth(formatTimer(35, 60, "§4Necron dropping in"), 1f, 1f, 2, Color.DARK_RED, shadow = true, center = false) * 2 + 2f to 16f
        } else if (necronTime >= 0) {
            mcTextAndWidth(formatTimer(necronTime.toInt(), 60, "§4Necron dropping in"), 1f, 1f, 2, Color.DARK_RED, shadow = true, center = false) * 2 + 2f to 16f
        } else 0f to 0f
    }

    private var necronTime: Byte = -1

    private val goldorHud by HudSetting("Goldor Hud", 10f, 10f, 1f, true) {
        if (it) {
            mcTextAndWidth(formatTimer(35, 60, "§7Tick:"), 1f, 1f, 2, Color.DARK_RED, shadow = true ,center = false) * 2 + 2f to 16f
        } else if ((goldorStartTime >= 0 && startTimer) || goldorTickTime >= 0) {
            val (prefix: String, time: Int, max: Int) = if (goldorStartTime >= 0 && startTimer) Triple("§aStart:", goldorStartTime, 104) else Triple("§7Tick:", goldorTickTime, 60)
            mcTextAndWidth(formatTimer(time, max, prefix), 1f, 1f, 2, Color.DARK_RED, shadow = true ,center = false) * 2 + 2f to 16f
        } else 0f to 0f
    }
    private val startTimer: Boolean by BooleanSetting("Start timer", default = false, description = "Displays a timer counting down until devices/terms are able to be activated/completed.").withDependency { goldorHud.enabled }

    private var goldorTickTime: Int = -1
    private var goldorStartTime: Int = -1

    private val stormHud by HudSetting("Storm Pad Hud", 10f, 10f, 1f, true) {
        if (it) {
            mcTextAndWidth(formatTimer(15, 20, "§bPad:"), 1f, 1f, 2, Color.DARK_RED, shadow = true ,center = false) * 2 + 2f to 16f
        } else if (padTickTime >= 0) {
            mcTextAndWidth(formatTimer(padTickTime, 20, "§bPad:"), 1f, 1f, 2, Color.DARK_RED, shadow = true ,center = false) * 2 + 2f to 16f
        } else 0f to 0f
    }

    private var padTickTime: Int = -1

    init {
        onMessage(Regex("\\[BOSS] Necron: I'm afraid, your journey ends now\\."), { enabled && necronHud.enabled }) { necronTime = 60 }

        onMessage(Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?"), { enabled && goldorHud.enabled }) { goldorTickTime = 60 }
        onMessage(Regex("The Core entrance is opening!"), { enabled && goldorHud.enabled }) {
            goldorTickTime = -1
            goldorStartTime = -1
        }

        onMessage(Regex("\\[BOSS] Storm: I should have known that I stood no chance\\.")) {
            if (goldorHud.enabled) goldorStartTime = 104
            if (stormHud.enabled) padTickTime = -1
        }

        onMessage(Regex("\\[BOSS] Storm: Pathetic Maxor, just like expected\\."), { enabled && stormHud.enabled }) { padTickTime = 20 }

        onWorldLoad {
            necronTime = -1

            goldorTickTime = -1
            goldorStartTime = -1

            padTickTime = -1
        }
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        if (necronTime >= 0 && necronHud.enabled) necronTime--

        if (goldorTickTime >= 0 && goldorHud.enabled) goldorTickTime--
        if (goldorStartTime >= 0 && goldorHud.enabled) goldorStartTime--

        if (goldorTickTime == 0 && goldorStartTime <= 0 && goldorHud.enabled) { goldorTickTime = 60 }

        if (padTickTime >= 0 && stormHud.enabled) padTickTime--
        if (padTickTime == 0 && stormHud.enabled) padTickTime = 20
    }

    private fun formatTimer(time: Int, max: Int, prefix: String): String {
        val color = when {
            time.toFloat() >= max * 0.66 -> "§a"
            time.toFloat() >= max * 0.33 -> "§6"
            else -> "§c"
        }
        val timeDisplay = if (displayInTicks) "$time${if (symbolDisplay) "t" else ""}" else "${String.format(Locale.US, "%.2f", time.toFloat() / 20)}${if (symbolDisplay) "s" else ""}"
        return "${if (showPrefix) "$prefix " else ""}$color$timeDisplay"
    }
}
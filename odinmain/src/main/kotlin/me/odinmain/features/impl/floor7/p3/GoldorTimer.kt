package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.SelectorSetting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GoldorTimer : Module(
    name = "Goldor Timer",
    description = "Displays the time until the Goldor fight starts in floor 7."
) {
    private val startTimer by BooleanSetting("Start Timer", default = true, description = "5 second countdown until terms/devices are able to be completed")
    private val displayText by BooleanSetting("Display Text", default = true, description = "Displays \"Start\"/\"Tick\" before the count")
    private val displayInTicks by SelectorSetting("Timer Style", "Seconds", arrayListOf("Seconds", "Ticks"), description = "Which Style to display timer in")
    private val symbolDisplay by BooleanSetting("Display Symbol", default = true, description = "Displays s or t after the time")
    private val showPrefix by BooleanSetting("Show Prefix", default = true, description = "Shows the prefix of the timer")
    /*private val hud: HudElement by HudSetting("Timer Hud", 10f, 10f, 1f, false) {
        if (it) {
            mcText("${if(showPrefix) "§7Tick: " else ""}§a59t", 1f, 1f, 1, Color.WHITE, center = false)
            getMCTextWidth("Tick: 59t") + 2f to 10f
        } else {
            val displayType = if (startTime >= 0) { startTime } else { tickTime }
            val colorCode = when {
                displayType >= 40 -> "§a"
                displayType in 20..40 -> "§6"
                displayType in 0..20 -> "§c"
                else -> return@HudSetting 0f to 0f
            }
            val text = when {
                (!displayText) -> ""
                (startTime >= 0)  -> "§aStart: "
                else -> "§8Tick: "
            }
            val displayTimer = if (!displayInTicks) { String.format("%.2f", displayType.toFloat() / 20) } else displayType
            val displaySymbol = when {
                (!displayInTicks && symbolDisplay) -> "s"
                (displayInTicks && symbolDisplay) -> "t"
                else -> ""
            }

            mcText("${if (showPrefix) text else ""}${colorCode}${displayTimer}${displaySymbol}", 1f, 1f, 1, Color.WHITE, center = false)
            getMCTextWidth("${text}${colorCode}${displayTimer}${displaySymbol}") + 2f to 10f
        }
    }*/

    private var tickTime = 0
    private var startTime = 0
    private val preStartRegex = Regex("\\[BOSS] Storm: I should have known that I stood no chance\\.")
    private val startRegex = Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?")
    private val endRegex = Regex("The Core entrance is opening!")

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        if (tickTime >= -1) tickTime--
        if (startTime >= -1) startTime--

        if (tickTime in -1..0 && startTime <= 0) { tickTime = 60 }
    }

    init {
        onWorldLoad {
            tickTime = -2
            startTime = -2
        }

        onMessage(Regex(".*")) {
            if (!it.matches(preStartRegex) && !it.matches(startRegex) && !it.matches(endRegex) || it.contains("Storm") && !startTimer) return@onMessage

            if (it.contains("Storm"))
                startTime = 104
            else if (it.contains("Core")) {
                tickTime = -2
                startTime = -2
            } else
                tickTime = 60
        }
    }
}
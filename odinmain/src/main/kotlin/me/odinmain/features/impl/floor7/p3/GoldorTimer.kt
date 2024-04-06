package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GoldorTimer : Module(
    name = "Goldor Timer",
    category = Category.FLOOR7,
    description = "Tick Timer for when goldor kills players"
) {
    private val startTimer: Boolean by BooleanSetting("Start Timer", default = true, description = "5 second countdown until terms/devices are able to be completed")
    private val displayText: Boolean by BooleanSetting("Display Text", default = true, description = "Displays \"Start\"/\"Tick\" before the count")
    private val displayInTicks: Boolean by BooleanSetting("Display in Ticks", default = false, description = "Displays the timer in game ticks rather than ms")
    private val symbolDisplay: Boolean by BooleanSetting("Display Symbol", default = true, description = "Displays s or t after the time")
    private val hud: HudElement by HudSetting("Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            text("§7Tick: §a59t", 1f, 9f, Color.RED, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Tick: 59t", 12f) + 2f to 16f
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

            text("${text}${colorCode}${displayTimer}${displaySymbol}", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("${text}${colorCode}${displayTimer}${displaySymbol}", 12f) + 2f to 12f
        }
    }

    private var tickTime = 0
    private var startTime = 0
    private var shouldLoad = false
    private val preStartRegex = Regex("\\[BOSS] Storm: I should have known that I stood no chance\\.")
    private val startRegex = Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?")
    private val endRegex = Regex("The Core entrance is opening!")

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        if (!shouldLoad) {
            tickTime = -2
            startTime = -2
            return
        }
        startTime--
        tickTime--

        if (tickTime in -1..0 && startTime <= 0) { tickTime = 60 }
    }

    init {
        onWorldLoad {
            shouldLoad = false
            tickTime = -2
            startTime = -2
        }

        onMessage(Regex(".*")) {
            if (!it.matches(preStartRegex) && !it.matches(startRegex) && !it.matches(endRegex) || it.contains("Storm") && !startTimer) return@onMessage
            if (it.contains("Core")) return@onMessage

            if (it.contains("Storm"))
                startTime = 104
            else
                tickTime = 60
        }
    }
}
package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.ChatPacketEvent
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
            val displayType = if (startTime.time >= 0) { startTime.time } else { tickTime.time }
            val colorCode = when {
                displayType >= 40 -> "§a"
                displayType in 20..40 -> "§6"
                displayType in 0..20 -> "§c"
                else -> return@HudSetting 0f to 0f
            }
            val text = when {
                (!displayText) -> ""
                (startTime.time >= 0)  -> "§aStart: "
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

    data class Timer(var time: Int)
    private val tickTime = Timer(0)
    private val startTime = Timer(0)
    private var shouldLoad = false
    private val preStartRegex = Regex("\\[BOSS] Storm: I should have known that I stood no chance\\.")
    private val startRegex = Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?")
    private val endRegex = Regex("The Core entrance is opening!")

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val msg = event.message
        if (!msg.matches(preStartRegex) && !msg.matches(startRegex) && !msg.matches(endRegex) || msg.contains("Storm") && !startTimer) return
        shouldLoad = !msg.contains("Core")
        if (!shouldLoad) return

        if (msg.contains("Storm")) {
            startTime.time = 104
        } else {
            tickTime.time = 60
        }
    }
    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        if (!shouldLoad) {
            tickTime.time = -2
            startTime.time = -2
            return
        }
        startTime.time--
        tickTime.time--

        if (tickTime.time in -1..0 && startTime.time <= 0) { tickTime.time = 60 }
    }

    init {
        onWorldLoad {
            shouldLoad = false
            tickTime.time = -2
            startTime.time = -2
        }
    }

}
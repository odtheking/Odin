package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.ListSetting
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import me.odinmain.utils.ui.getMCTextHeight
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

object Countdowns : Module(
    name = "Countdowns",
    description = "Starts a countdown in HUD when you receive certain chat messages." // "/countdowns (coming soon)"
) {
    private val hud by HUD("Hud", "Displays something i can't tell.", false) { example ->
        if (example) return@HUD drawStringWidth("Some Countdown: 3.50s", 1f, 1f, Colors.WHITE) to 10f

        var w = 1f
        var h = 1f
        val lineHeight = getMCTextHeight()

        countdowns.forEach {
            w = maxOf(w, drawStringWidth(
                "§r${it.prefix}${it.time.toFixed(divisor = 20)}",
                1f, w, Colors.WHITE
            ))
            h += lineHeight
        }

        if (h == 1f) (0f to 0f) else (w to h)
    }

    data class CountdownTrigger(val prefix: String, val time: Int, val message: String)
    val countdownTriggers by ListSetting("Countdowns", mutableListOf<CountdownTrigger>())

    private data class Countdown(val prefix: String, var time: Int)
    private val countdowns = CopyOnWriteArrayList<Countdown>()

    init {
        onMessage(Regex(".*")) { result ->
            countdownTriggers.firstOrNull {
                it.message == result.value
            }?.let {
                countdowns.add(Countdown(it.prefix, it.time))
            }
        }

        onWorldLoad {
            countdowns.clear()
        }
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        countdowns.removeIf {
            --it.time <= 0
        }
    }
}

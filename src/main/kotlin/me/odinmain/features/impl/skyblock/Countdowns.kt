package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.ListSetting
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

object Countdowns : Module(
    name = "Countdowns",
    description = "Starts a countdown in HUD when you receive certain chat messages." // "/countdowns (coming soon)"
) {
    private val hud by HUD("Hud", "Displays something i can't tell.", false) { example ->
        if (example) {
            "Some Timer: 4.30s"
        } else {
            countdowns.joinToString("\n") {
                "§r${it.prefix}${it.time.toFixed(divisor = 20)}"
            }
        }.let {
            if (it.isNotEmpty())
                drawStringWidth(it, 1f, 1f, Colors.WHITE) + 2f to 10f
            else
                0f to 0f
        }
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
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        countdowns.removeIf {
            --it.time <= 0
        }
    }
}

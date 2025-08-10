package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.SplitsManager.currentSplits
import me.odinmain.utils.SplitsManager.getAndUpdateSplitsTimes
import me.odinmain.utils.formatTime
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.getTextWidth

object Splits : Module(
    name = "Splits",
    description = "Provides visual timers for Kuudra and Dungeons."
) {
    private val hud by HUD("Splits Display HUD", "Shows timers for each split.") { example ->
        if (example) {
            repeat(5) { i ->
                RenderUtils.drawText("Split $i: 0h 00m 00s" + if (showTickTime) " §7(§80s§7)" else "", 1f, 1f + i * 9f, Colors.WHITE, shadow = true)
            }
            return@HUD getTextWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0s)" else "") + 2f to 9f * 5
        }

        val (times, tickTimes, current) = getAndUpdateSplitsTimes(currentSplits)
        if (currentSplits.splits.isEmpty()) return@HUD 0f to 0f

        val maxWidth = getTextWidth("Scarf's minions: ")

        currentSplits.splits.dropLast(1).forEachIndexed { index, split ->
            val time = formatTime(if (index >= times.size) 0 else times[index], numbersAfterDecimal)
            RenderUtils.drawText(split.name, 1f, 1f + index * 9f, Colors.WHITE, shadow = true)

            val displayText = if (showTickTime && index < tickTimes.size) "$time §7(§8${(tickTimes[index] / 20f).toFixed()}§7)" else time

            RenderUtils.drawText(displayText, maxWidth + 5f, 1f + index * 9f, Colors.WHITE, shadow = true)
        }

        if (bossEntrySplit && currentSplits.splits.size > 3) {
            RenderUtils.drawText("§9Boss Entry", 1f, 1f + (currentSplits.splits.size - 1) * 9f, Colors.WHITE, shadow = true)

            val totalTime = formatTime(times.take(3).sum(), numbersAfterDecimal)
            val displayText = if (showTickTime) "$totalTime §7(§8${(tickTimes.take(3).sum() / 20f).toFixed()}§7)" else totalTime

            RenderUtils.drawText(displayText, maxWidth + 5f, 1f + (currentSplits.splits.size - 1) * 9f, Colors.WHITE, shadow = true)
        }

        getTextWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2f to 9f * (currentSplits.splits.size + (if (bossEntrySplit) 1 else 0))
    }

    private val bossEntrySplit by BooleanSetting("Boss Entry Split", true, desc = "Split for boss entry.")
    val sendSplits by BooleanSetting("Send Splits", true, desc = "Send splits to chat.")
    val sendOnlyPB by BooleanSetting("Send Only PB", false, desc = "Send only personal bests.")
    private val numbersAfterDecimal by NumberSetting("Numbers After Decimal", 2, 0, 5, 1, desc = "Numbers after decimal in time.")
    val showTickTime by BooleanSetting("Show Tick Time", false, desc = "Show tick-based time alongside real time.")
}
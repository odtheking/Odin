package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.features.Module
import me.odinmain.utils.SplitsManager.currentSplits
import me.odinmain.utils.SplitsManager.getAndUpdateSplitsTimes
import me.odinmain.utils.formatTime
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.ui.getTextWidth

object Splits : Module(
    name = "Splits",
    description = "Provides visual timers for Kuudra and Dungeons."
) {
    private val hud by HUD("Splits Display HUD", "Shows timers for each split.") { example ->
        if (example) {
            repeat(5) { i ->
                RenderUtils.drawText("Split $i: 0h 00m 00s", 1f, 1f + i * 9f, Colors.WHITE, shadow = true)
            }
            return@HUD getTextWidth("Split 0: 0h 00m 00s") + 2f to 10f * 5
        }

        val (times, current) = getAndUpdateSplitsTimes(currentSplits)
        if (currentSplits.splits.isEmpty()) return@HUD 0f to 0f
        val x = getTextWidth("Professor: 0m 00s")
        currentSplits.splits.dropLast(1).forEachIndexed { index, split ->
            val seconds = times[index] / 20f
            val time = formatTime((seconds * 1000).toLong())
            RenderUtils.drawText(split.name, 1f, 1f + index * 9f, Colors.WHITE, shadow = true)
            RenderUtils.drawText(time, x.toFloat(), 1f + index * 9f, Colors.WHITE, shadow = true)
        }
        if (bossEntrySplit && currentSplits.splits.size > 3) {
            RenderUtils.drawText("§9Boss Entry", 1f, (currentSplits.splits.size - 1) * 9f, Colors.WHITE, shadow = true)
            RenderUtils.drawText(formatTime(((times.take(3).sum() / 20f) * 1000).toLong()), x.toFloat(), (currentSplits.splits.size - 1) * 9f, Colors.WHITE, shadow = true)
        }
        getTextWidth("Split 0: 0h 00m 00s") + 2f to 9f * 5
    }
    private val bossEntrySplit by BooleanSetting("Boss Entry Split", true, desc = "Split for boss entry.")
    val sendSplits by BooleanSetting("Send Splits", true, desc = "Send splits to chat.")
    val sendOnlyPB by BooleanSetting("Send Only PB", false, desc = "Send only personal bests.")
}
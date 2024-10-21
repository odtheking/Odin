package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.SplitsManager.currentSplits
import me.odinmain.utils.SplitsManager.getAndUpdateSplitsTimes
import me.odinmain.utils.formatTime
import me.odinmain.utils.render.*

object Splits : Module(
    name = "Splits",
    description = "Automatic advanced skyblock splits.",
    category = Category.SKYBLOCK
) {
    private val hud by HudSetting("Splits Display HUD", 10f, 10f, 1f, true) { example ->
        if (example) {
            repeat(5) { i ->
                mcText("Split $i:", 1f, 9f + i * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
            }
            return@HudSetting getMCTextWidth("Split 0: 0h 00m 00s") + 2f to 80f
        }

        val (times, current) = getAndUpdateSplitsTimes(currentSplits)
        if (currentSplits.splits.isEmpty()) return@HudSetting 0f to 0f
        val x = getMCTextWidth("Professor: 0h 00m 00s")
        currentSplits.splits.dropLast(1).forEachIndexed { index, split ->
            val time = formatTime(if (index >= times.size) 0 else times[index], numbersAfterDecimal)
            mcText(split.name, 1f, 9f + index * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
            mcText(time, x, 9f + index * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
        }
        if (bossEntrySplit && currentSplits.splits.size > 3) {
            mcText("§9Boss Entry", 1f, (currentSplits.splits.size) * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
            mcText(formatTime(times.take(3).sum(), numbersAfterDecimal), x, (currentSplits.splits.size) * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
        }
        getMCTextWidth("Split 0: 0h 00m 00s") + 2f to 80f
    }
    private val bossEntrySplit by BooleanSetting("Boss Entry Split", true, description = "Split for boss entry.")
    val sendSplits by BooleanSetting("Send Splits", true, description = "Send splits to chat.")
    val sendOnlyPB by BooleanSetting("Send Only PB", false, description = "Send only personal bests.")
    private val numbersAfterDecimal by NumberSetting("Numbers After Decimal", 2, 0, 5, 1, description = "Numbers after decimal in time.")
}
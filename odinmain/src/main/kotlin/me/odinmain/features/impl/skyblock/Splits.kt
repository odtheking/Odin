package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.SplitsManager.currentSplits
import me.odinmain.utils.SplitsManager.getAndUpdateSplitsTimes
import me.odinmain.utils.formatTime
import me.odinmain.utils.render.*

object Splits : Module(
    name = "Splits",
    description = "Automatic advanced skyblock splits.",
    category = Category.SKYBLOCK
) {
    private val hud: HudElement by HudSetting("Splits Display HUD", 10f, 10f, 1f, true) {
        if (it) {
            for (i in 0 until 5) {
                text("Split $i: 0h 00m 00s", 1f, 9f + i * getTextHeight("12", 13f), Color.WHITE, 12f, shadow = true)
            }

            getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) + 2f to 80f
        } else {
            val (times, current) = getAndUpdateSplitsTimes(currentSplits)
            if (currentSplits.splits.isEmpty()) return@HudSetting 0f to 0f
            val x = getMCTextWidth("Split: 0h 00m 00s")
            currentSplits.splits.dropLast(1).forEachIndexed { index, split ->
                val time = formatTime(if (index >= times.size) 0 else times[index])
                mcText(split.name, 1f, 9f + index * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
                mcText(time, x, 9f + index * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
            }
            if (bossEntrySplit && currentSplits.splits.size > 3) {
                val time = formatTime(times.take(3).sum())
                mcText("§9Boss Entry", 1f, (currentSplits.splits.size) * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
                mcText(time, x, (currentSplits.splits.size) * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
            }
            getMCTextWidth("Split 0: 0h 00m 00s") + 2f to 80f
        }
    }
    private val bossEntrySplit: Boolean by BooleanSetting("Boss Entry Split", true)
    val sendSplits: Boolean by BooleanSetting("Send Splits", true)
    val sendOnlyPB: Boolean by BooleanSetting("Send Only PB", false)
}
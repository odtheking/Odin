package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.PersonalBest
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.SplitsManager.currentSplits
import com.odtheking.odin.utils.skyblock.SplitsManager.getAndUpdateSplitsTimes
import com.odtheking.odin.utils.toFixed

object Splits : Module(
    name = "Splits",
    description = "Provides visual timers for Kuudra and Dungeons."
) {
    private val hud by HUD("Splits Display HUD", "Shows timers for each split.") { example ->
        val totalWidth = getStringWidth("Split 0: 0h 00m 00s" + if (showTickTime) " (0h 00m 00s)" else "") + 2

        if (example) {
            repeat(5) { i ->
                val exampleTime = "0h 00m 00s" + if (showTickTime) " §8(§70s§8)" else ""
                if (fixedWidth) {
                    text("Split $i:", 0, i * 9, Colors.WHITE)
                    text(exampleTime, totalWidth - getStringWidth("0h 00m 00s" + if (showTickTime) " (0s)" else ""), i * 9, Colors.WHITE)
                } else {
                    text("Split $i: $exampleTime", 0, i * 9, Colors.WHITE)
                }
            }
            return@HUD totalWidth to 9 * 5
        }

        val (times, tickTimes, current) = getAndUpdateSplitsTimes(currentSplits)
        if (currentSplits.splits.isEmpty()) return@HUD 0 to 0

        val maxWidth = currentSplits.splits.dropLast(1).maxOf { getStringWidth(it.name) }

        currentSplits.splits.dropLast(1).forEachIndexed { index, split ->
            val time = formatTime(if (index >= times.size) 0 else times[index])
            text(split.name, 0, index * 9, Colors.WHITE)

            val displayText = if (showTickTime && index < tickTimes.size) "$time §8(§7${(tickTimes[index] / 20f).toFixed()}§8)" else time
            val timeX = if (fixedWidth) totalWidth - getStringWidth(displayText) else maxWidth + 4

            text(displayText, timeX, index * 9, Colors.WHITE)
        }

        if (bossEntrySplit && currentSplits.splits.size > 3) {
            text("§9Boss Entry", 0, (currentSplits.splits.size - 1) * 9, Colors.WHITE)

            val totalTime = formatTime(times.take(3).sum())
            val displayText = if (showTickTime) "$totalTime §8(§7${(tickTimes.take(3).sum() / 20f).toFixed()}§8)" else totalTime
            val timeX = if (fixedWidth) totalWidth - getStringWidth(displayText) else maxWidth + 4

            text(displayText, timeX, (currentSplits.splits.size - 1) * 9, Colors.WHITE)
        }

        totalWidth to 9 * (currentSplits.splits.size + (if (bossEntrySplit) 1 else 0))
    }

    private val currentSplitHud by HUD("Current Split HUD", "Shows only the current split and its tick time.") { example ->
        if (example) {
            val exampleText = "§70s"
            val w = getStringWidth(exampleText)
            text(exampleText, 0, 0, Colors.WHITE)
            return@HUD w to 9
        }

        val splits = currentSplits.splits
        if (splits.isEmpty()) return@HUD 0 to 0

        val (_, tickTimes, current) = getAndUpdateSplitsTimes(currentSplits)
        if (current !in splits.indices || current >= tickTimes.size) return@HUD 0 to 0

        val displayText = "§7${(tickTimes[current] / 20f).toFixed()}s"
        val w = getStringWidth(displayText) + 2
        text(displayText, -w / 2, 0, Colors.WHITE)
        w to 9
    }

    private val fixedWidth by BooleanSetting("Fixed Width", true, desc = "Always use a fixed HUD width, right-aligning the times.")
    private val bossEntrySplit by BooleanSetting("Boss Entry Split", true, desc = "Split for boss entry.")
    val sendSplits by BooleanSetting("Send Splits", true, desc = "Send splits to chat.")
    val showTickTime by BooleanSetting("Show Tick Time", true, desc = "Show tick-based time alongside real time.")
    val splitLocation by SelectorSetting("Split Location", "Both", listOf("Both", "Dungeons Only", "Kuudra Only"), desc = "Which areas to show splits in.")

    val kuudraT5PBs = PersonalBest(this, "KuudraT5")
    val kuudraT4PBs = PersonalBest(this, "KuudraT4")
    val kuudraT3PBs = PersonalBest(this, "KuudraT3")
    val kuudraT2PBs = PersonalBest(this, "KuudraT2")
    val kuudraT1PBs = PersonalBest(this, "KuudraT1")

    private val dungeonEPBs = PersonalBest(this, "DungeonE")
    private val dungeonF1PBs = PersonalBest(this, "DungeonF1")
    private val dungeonF2PBs = PersonalBest(this, "DungeonF2")
    private val dungeonF3PBs = PersonalBest(this, "DungeonF3")
    private val dungeonF4PBs = PersonalBest(this, "DungeonF4")
    private val dungeonF5PBs = PersonalBest(this, "DungeonF5")
    private val dungeonF6PBs = PersonalBest(this, "DungeonF6")
    private val dungeonF7PBs = PersonalBest(this, "DungeonF7")

    private val dungeonM1PBs = PersonalBest(this, "DungeonM1")
    private val dungeonM2PBs = PersonalBest(this, "DungeonM2")
    private val dungeonM3PBs = PersonalBest(this, "DungeonM3")
    private val dungeonM4PBs = PersonalBest(this, "DungeonM4")
    private val dungeonM5PBs = PersonalBest(this, "DungeonM5")
    private val dungeonM6PBs = PersonalBest(this, "DungeonM6")
    private val dungeonM7PBs = PersonalBest(this, "DungeonM7")

    val dungeonPBsList = listOf(dungeonEPBs, dungeonF1PBs, dungeonF2PBs, dungeonF3PBs, dungeonF4PBs, dungeonF5PBs, dungeonF6PBs, dungeonF7PBs,
        dungeonM1PBs, dungeonM2PBs, dungeonM3PBs, dungeonM4PBs, dungeonM5PBs, dungeonM6PBs, dungeonM7PBs)
}
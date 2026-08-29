package com.odtheking.odin.features.impl.skyblock

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.PersonalBest
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.SplitsManager.currentRows
import com.odtheking.odin.utils.skyblock.floor7SplitGroup
import com.odtheking.odin.utils.toFixed

object Splits : Module(
    name = "Splits",
    description = "Provides visual timers for Kuudra and Dungeons."
) {
    private val hud by HUD("Splits Display HUD", "Shows timers for each split.") { example ->
        val timeWidth = 75 + if (showTickTime) 20 else -20

        if (example) {
            val labelWidth = floor7SplitGroup.maxOf { getStringWidth("${it.name}:") }
            val timeExample = "59m 59s" + if (showTickTime) " §8(§759.9§8)" else ""
            val totalWidth = labelWidth + 4 + timeWidth + 2

            floor7SplitGroup.forEachIndexed { i, split ->
                if (fixedWidth) {
                    text("${split.name}:", 0, i * 9, Colors.WHITE)
                    text(timeExample, totalWidth - getStringWidth(timeExample) - 2, i * 9, Colors.WHITE)
                } else text("${split.name}: $timeExample", 0, i * 9, Colors.WHITE)
            }
            return@HUD totalWidth to 9 * floor7SplitGroup.size
        }

        val rows = currentRows()
        if (rows.isEmpty()) return@HUD 0 to 0
        val segments = rows.dropLast(1)

        val labelWidth = segments.maxOfOrNull { getStringWidth(it.name) } ?: 0
        val totalWidth = labelWidth + 4 + timeWidth + 2

        segments.forEachIndexed { index, row ->
            if (row.time == 0L && !show0Time) return@forEachIndexed
            val time = formatTime(row.time)
            text(row.name, 0, index * 9, Colors.WHITE)

            val displayText = if (showTickTime) "$time §8(§7${(row.tickTime / 20f).toFixed()}§8)" else time

            val timeX = if (fixedWidth) labelWidth + 4 + timeWidth - getStringWidth(displayText)
            else labelWidth + 4

            text(displayText, timeX, index * 9, Colors.WHITE)
        }

        if (bossEntrySplit && rows.size > 3) {
            val y = segments.size * 9
            text("§9Boss Entry", 0, y, Colors.WHITE)

            val totalTime = formatTime(segments.take(3).sumOf { it.time })
            val displayText = if (showTickTime) "$totalTime §8(§7${(segments.take(3).sumOf { it.tickTime } / 20f).toFixed()}§8)"
            else totalTime

            val timeX = if (fixedWidth) labelWidth + 4 + timeWidth - getStringWidth(displayText)
            else labelWidth + 4

            text(displayText, timeX, y, Colors.WHITE)
        }

        totalWidth to 9 * (rows.size + (if (bossEntrySplit) 1 else 0))
    }

    private val currentSplitHud by HUD("Current Split HUD", "Shows only the current split and its tick time.") { example ->
        if (example) {
            val exampleText = "§70s"
            val w = getStringWidth(exampleText)
            text(exampleText, 0, 0, Colors.WHITE)
            return@HUD w to 9
        }

        val current = currentRows().find { it.isCurrent } ?: return@HUD 0 to 0

        val displayText = "§7${(current.tickTime / 20f).toFixed()}s"
        val w = getStringWidth(displayText) + 2
        text(displayText, -w / 2, 0, Colors.WHITE)
        w to 9
    }

    private val fixedWidth by BooleanSetting("Fixed Width", true, desc = "Always use a fixed HUD width, right-aligning the times.")
    private val bossEntrySplit by BooleanSetting("Boss Entry Split", true, desc = "Split for boss entry.")
    private val show0Time by BooleanSetting("Show 0 splits", false, desc = "Shows splits which have their time at 0.")
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
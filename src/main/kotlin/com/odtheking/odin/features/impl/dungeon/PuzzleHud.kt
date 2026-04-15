package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.Puzzle
import com.odtheking.odin.utils.skyblock.dungeon.PuzzleStatus

object PuzzleHud : Module(
    name = "Puzzle HUD",
    description = "Displays remaining, completed, and failed puzzles in the HUD during dungeons."
) {
    private val showPlayerNames by BooleanSetting("Show Player Names", true, desc = "Shows which player solved or failed the puzzle.")

    private val hud by HUD("Puzzle HUD Position", "Displays dungeon puzzle statuses on the HUD.") { example ->
        if (!DungeonUtils.inDungeons && !example) return@HUD 0 to 0

        val puzzleCount = if (example) 4 else DungeonUtils.puzzleCount
        if (puzzleCount == 0 && !example) return@HUD 0 to 0

        data class PuzzleEntry(val name: String, val statusIcon: String, val statusColor: String, val player: String?)

        val entries: List<PuzzleEntry> = if (example) {
            listOf(
                PuzzleEntry(Puzzle.BLAZE.displayName, "✖", "§c", "ItsAkar1881"),
                PuzzleEntry(Puzzle.BEAMS.displayName, "✔", "§a", "Player123"),
                PuzzleEntry(Puzzle.UNKNOWN.displayName, "✦", "§6", null),
                PuzzleEntry(Puzzle.UNKNOWN.displayName, "✦", "§6", null)
            )
        } else {
            val discovered = DungeonUtils.puzzles.map { puzzle ->
                val (icon, color) = when (puzzle.status) {
                    PuzzleStatus.Completed -> "✔" to "§a"
                    PuzzleStatus.Failed -> "✖" to "§c"
                    PuzzleStatus.Incomplete -> "✦" to "§6"
                    null -> "✦" to "§6"
                }
                PuzzleEntry(puzzle.displayName, icon, color, puzzle.player)
            }
            val undiscoveredCount = (puzzleCount - discovered.size).coerceAtLeast(0)
            val undiscovered = (1..undiscoveredCount).map {
                PuzzleEntry("???", "✦", "§6", null)
            }
            discovered + undiscovered
        }

        var width = 0
        val lineHeight = 12

        val headerText = "§fPuzzles: (§3$puzzleCount§f)"
        text(headerText, 0, 0, Colors.WHITE)
        getStringWidth("Puzzles: ($puzzleCount)").let { if (it > width) width = it }

        entries.forEachIndexed { index, entry ->
            val y = (index + 1) * lineHeight
            val playerText = if (showPlayerNames && entry.player != null) " §7(${entry.player})" else ""
            val line = "§f${entry.name}: [${entry.statusColor}${entry.statusIcon}§f]$playerText"
            text(line, 0, y, Colors.WHITE)
            getStringWidth("${entry.name}: [${entry.statusIcon}]${if (showPlayerNames && entry.player != null) " (${entry.player})" else ""}").let { if (it > width) width = it }
        }

        val totalLines = entries.size + 1
        width to totalLines * lineHeight
    }
}

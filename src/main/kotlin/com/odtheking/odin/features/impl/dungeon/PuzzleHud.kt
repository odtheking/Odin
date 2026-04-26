package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.Puzzle
import com.odtheking.odin.utils.skyblock.dungeon.PuzzleStatus

object PuzzleHud : Module(
    name = "Puzzle HUD",
    description = "Displays remaining, completed, and failed puzzles in the HUD during dungeons."
) {
    private val showPlayerNames by BooleanSetting("Show Player Names", true, desc = "Shows which player solved or failed the puzzle.")

    private data class PuzzleEntry(val name: String, val statusIcon: String, val statusColor: String, val player: String?)
    private val examplePuzzles = listOf(
        PuzzleEntry(Puzzle.ICE_PATH.displayName, "✖", "§c", "odtheking"),
        PuzzleEntry(Puzzle.BEAMS.displayName, "✔", "§a", null),
        PuzzleEntry(Puzzle.UNKNOWN.displayName, "✦", "§6", null),
        PuzzleEntry(Puzzle.UNKNOWN.displayName, "✦", "§6", null)
    )

    private val hud by HUD("Puzzle HUD Position", "Displays dungeon puzzle statuses on the HUD.") { example ->
        if (!DungeonUtils.inDungeons && !example) return@HUD 0 to 0

        val puzzleCount = if (example) 4 else DungeonUtils.puzzleCount
        if (puzzleCount == 0) return@HUD 0 to 0

        val entries = if (example) examplePuzzles
        else {
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
            val undiscovered = (1..undiscoveredCount).map { PuzzleEntry("???", "✦", "§6", null) }
            discovered + undiscovered
        }

        var width = 0
        val lineHeight = 12

        val headerText = "§5Puzzles §8(§3$puzzleCount§8)"
        width = textDim(headerText, 0, 0, Colors.WHITE).first

        entries.forEachIndexed { index, entry ->
            val playerText = if (showPlayerNames && entry.player != null) " §7(${entry.player})" else ""
            val newWidth = textDim("§e${entry.name}§8: [${entry.statusColor}${entry.statusIcon}§8]$playerText", 0, (index + 1) * lineHeight, Colors.WHITE).first
            if (newWidth > width) width = newWidth
        }

        val totalLines = entries.size + 1
        width to totalLines * lineHeight
    }
}

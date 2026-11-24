package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.Puzzle
import com.odtheking.odin.utils.skyblock.dungeon.PuzzleStatus
import net.minecraft.client.gui.GuiGraphicsExtractor

object PuzzleHud : Module(
    name = "Puzzle HUD",
    description = "Displays remaining, completed, and failed puzzles in the HUD during dungeons."
) {
    private val showPlayerNames by BooleanSetting("Show Player Names", true, desc = "Shows which player solved or failed the puzzle.")
    private val showOnlyPre by BooleanSetting("Show Only Pre-Run", false, desc = "Shows only puzzles that are before the boss room.")

    private data class PuzzleEntry(val name: String, val statusIcon: String, val statusColor: String, val player: String?)
    private val examplePuzzles = listOf(
        PuzzleEntry(Puzzle.ICE_PATH.displayName, "✖", "§c", "odtheking"),
        PuzzleEntry(Puzzle.BEAMS.displayName, "✔", "§a", null),
        PuzzleEntry(Puzzle.UNKNOWN.displayName, "✦", "§6", null),
        PuzzleEntry(Puzzle.UNKNOWN.displayName, "✦", "§6", null)
    )

    private val hud by HUD("Puzzle HUD", "Displays dungeon puzzle statuses on the HUD.", false, example = {
        drawPuzzles(examplePuzzles.size, examplePuzzles)
        maxOf(getStringWidth(headerText(examplePuzzles.size)), examplePuzzles.maxOf { getStringWidth(entryText(it)) }) to (examplePuzzles.size + 1) * LINE_HEIGHT
    }) {
        if (!DungeonUtils.inClear || (showOnlyPre && DungeonUtils.openRoomCount != 0)) return@HUD

        val puzzleCount = DungeonUtils.puzzleCount
        if (puzzleCount == 0) return@HUD

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
        drawPuzzles(puzzleCount, discovered + undiscovered)
    }

    private const val LINE_HEIGHT = 12

    private fun headerText(puzzleCount: Int) = "§5Puzzles §8(§3$puzzleCount§8)"

    private fun entryText(entry: PuzzleEntry): String {
        val playerText = if (showPlayerNames && entry.player != null) " §7(§6${entry.player}§7)" else ""
        return "§e${entry.name}§8: [${entry.statusColor}${entry.statusIcon}§8]$playerText"
    }

    private fun GuiGraphicsExtractor.drawPuzzles(puzzleCount: Int, entries: List<PuzzleEntry>) {
        text(headerText(puzzleCount), 0, 0, Colors.WHITE)
        entries.forEachIndexed { index, entry -> text(entryText(entry), 0, (index + 1) * LINE_HEIGHT, Colors.WHITE) }
    }
}

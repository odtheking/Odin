package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.render.Color

object PuzzleSolvers : Module(
    name = "Puzzle Solvers",
    category = Category.DUNGEON,
    description = "Dungeon puzzle solvers."
) {

    val waterSolver: Boolean by BooleanSetting("Water Board", true, description = "Shows you the solution to the water puzzle.")
    val showOrder: Boolean by BooleanSetting("Show Order", true, description = "Shows the order of the levers to click.").withDependency { waterSolver }
    val reset: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        WaterSolver.reset()
    }.withDependency { waterSolver }

    val tpMaze: Boolean by BooleanSetting("Teleport Maze", true, description = "Shows you the solution for the TP maze puzzle")
    val mazeColorOne: Color by ColorSetting("Color for 1 solution", Color.GREEN, true).withDependency { tpMaze }
    val mazeColorMultiple: Color by ColorSetting("Color for multiple solutions", Color.ORANGE, true).withDependency { tpMaze }

    val tttSolver: Boolean by BooleanSetting("Tic Tac Toe", true, description = "Shows you the solution for the TTT puzzle")
    val blockWrongClicks: Boolean by BooleanSetting(name = "Block Wrong Clicks").withDependency { tttSolver && !OdinMain.onLegitVersion }

}
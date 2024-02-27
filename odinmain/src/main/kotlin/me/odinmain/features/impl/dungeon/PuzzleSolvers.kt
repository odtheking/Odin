package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain
import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.WaterSolver.waterInteract
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

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
    val mazeColorOne: Color by ColorSetting("Color for 1 solution", Color.GREEN.withAlpha(.5f), true).withDependency { tpMaze }
    val mazeColorMultiple: Color by ColorSetting("Color for multiple solutions", Color.ORANGE.withAlpha(.5f), true).withDependency { tpMaze }
    val mazeColorVisited: Color by ColorSetting("Color for visited", Color.RED.withAlpha(.5f), true).withDependency { tpMaze }

    val tttSolver: Boolean by BooleanSetting("Tic Tac Toe", true, description = "Shows you the solution for the TTT puzzle")
    val blockWrongClicks: Boolean by BooleanSetting(name = "Block Wrong Clicks").withDependency { tttSolver && !OdinMain.onLegitVersion }

    init {
        execute(1000) {
            WaterSolver.scan()
        }

        execute(200) {
            TPMaze.scan()
        }

        onPacket(S08PacketPlayerPosLook::class.java) {
            TPMaze.tpPacket(it)
        }

        onWorldLoad {
            WaterSolver.reset()
            TPMaze.portals = setOf()
            TPMaze.correctPortals = listOf()
            TicTacToe.reset()
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        WaterSolver.waterRender()
        TPMaze.tpRender()
        TicTacToe.tttRender()
    }

    @SubscribeEvent
    fun onBlockInteract(event: PlayerInteractEvent) {
        waterInteract(event)
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        TicTacToe.tttTick(event)
    }

    @SubscribeEvent
    fun onRightClick(event: ClickEvent.RightClickEvent) {
        TicTacToe.tttRightClick(event)
    }

}
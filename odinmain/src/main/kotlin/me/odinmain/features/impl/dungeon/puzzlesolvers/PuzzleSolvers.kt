package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.puzzlesolvers.WaterSolver.waterInteract
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.profile
import me.odinmain.utils.render.Color
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object PuzzleSolvers : Module(
    name = "Puzzle Solvers",
    category = Category.DUNGEON,
    description = "Displays solutions for dungeon puzzles.",
    key = null
) {
    private val waterDropDown: Boolean by DropdownSetting("Water")
    private val waterSolver: Boolean by BooleanSetting("Water Board", false, description = "Shows you the solution to the water puzzle.").withDependency { waterDropDown }
    val showOrder: Boolean by BooleanSetting("Show Order", true, description = "Shows the order of the levers to click.").withDependency { waterSolver && waterDropDown }
    val showTracer: Boolean by BooleanSetting("Show Tracer", true, description = "Shows a tracer to the next lever.").withDependency { waterSolver && waterDropDown }
    val tracerColorFirst: Color by ColorSetting("Tracer Color First", Color.GREEN, true, description = "Color for the first tracer").withDependency { showTracer && waterDropDown }
    val tracerColorSecond: Color by ColorSetting("Tracer Color Second", Color.ORANGE, true, description = "Color for the second tracer").withDependency { showTracer && waterDropDown }
    val reset: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        WaterSolver.reset()
    }.withDependency { waterSolver && waterDropDown }

    private val mazeDropDown: Boolean by DropdownSetting("Maze")
    private val tpMaze: Boolean by BooleanSetting("Teleport Maze", false, description = "Shows you the solution for the TP maze puzzle").withDependency { mazeDropDown }
    val solutionThroughWalls: Boolean by BooleanSetting("Solution through walls", false, description = "Renders the final solution through walls").withDependency { tpMaze && mazeDropDown }
    val mazeColorOne: Color by ColorSetting("Color for one solution", Color.GREEN.withAlpha(.5f), true, description = "Color for when there is a single solution").withDependency { tpMaze && mazeDropDown }
    val mazeColorMultiple: Color by ColorSetting("Color for multiple solutions", Color.ORANGE.withAlpha(.5f), true, description = "Color for when there are multiple solutions").withDependency { tpMaze && mazeDropDown }
    val mazeColorVisited: Color by ColorSetting("Color for visited", Color.RED.withAlpha(.5f), true, description = "Color for the already used TP pads").withDependency { tpMaze && mazeDropDown }
    private val click: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        TPMaze.reset()
    }.withDependency { tpMaze && mazeDropDown }

    private val tttDropDown: Boolean by DropdownSetting("Tic Tac Toe")
    private val tttSolver: Boolean by BooleanSetting("Tic Tac Toe", false, description = "Shows you the solution for the TTT puzzle").withDependency { tttDropDown }
    val tttColor: Color by ColorSetting("TTT Color", Color.GREEN, true, description = "Color for the tic tac toe solver").withDependency { tttSolver && tttDropDown }
    val tttStyle: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { tttSolver && tttDropDown }

    private val iceFillDropDown: Boolean by DropdownSetting("Ice Fill")
    private val iceFillSolver: Boolean by BooleanSetting("Ice Fill Solver", false, description = "Solver for the ice fill puzzle").withDependency { iceFillDropDown }
    private val iceFillColor: Color by ColorSetting("Ice Fill Color", Color.PINK, true, description = "Color for the ice fill solver").withDependency { iceFillSolver && iceFillDropDown }
    val action: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        IceFillSolver.reset()
    }.withDependency { iceFillSolver && iceFillDropDown }

    private val blazeDropDown: Boolean by DropdownSetting("Blaze")
    private val blazeSolver: Boolean by BooleanSetting("Blaze Solver").withDependency { blazeDropDown }
    val blazeLineNext: Boolean by BooleanSetting("Blaze Solver Next Line", true).withDependency { blazeSolver && blazeDropDown }
    val blazeLineAmount: Int by NumberSetting("Blaze Solver Lines", 1, 1, 10).withDependency { blazeSolver && blazeLineNext && blazeDropDown }
    val blazeStyle: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { blazeSolver && blazeDropDown }
    val blazeFirstColor: Color by ColorSetting("First Color", Color.GREEN, true).withDependency { blazeSolver && blazeDropDown }
    val blazeSecondColor: Color by ColorSetting("Second Color", Color.ORANGE, true).withDependency { blazeSolver && blazeDropDown }
    val blazeAllColor: Color by ColorSetting("Other Color", Color.WHITE.withAlpha(.3f), true).withDependency { blazeSolver && blazeDropDown }
    val blazeSendComplete: Boolean by BooleanSetting("Send Complete", false, description = "Send complete message").withDependency { blazeSolver && blazeDropDown }
    private val blazeReset: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        BlazeSolver.reset()
    }.withDependency { blazeSolver && blazeDropDown }

    private val beamsDropDown: Boolean by DropdownSetting("Creeper Beams")
    private val beamsSolver: Boolean by BooleanSetting("Creeper Beams", false, description = "Shows you the solution for the Creeper Beams puzzle").withDependency { beamsDropDown }
    val beamStyle: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { beamsSolver && beamsDropDown }
    val beamsDepth: Boolean by BooleanSetting("Depth", false, description = "Depth check").withDependency { beamsSolver && beamsDropDown }
    val beamsTracer: Boolean by BooleanSetting("Tracer", false, description = "Tracer").withDependency { beamsSolver && beamsDropDown }
    private val beamsReset: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        BeamsSolver.reset()
    }.withDependency { beamsSolver && beamsDropDown }

    private val weirdosDropDown: Boolean by DropdownSetting("Weirdos")
    private val weirdosSolver: Boolean by BooleanSetting("Weirdos", false, description = "Shows you the solution for the Weirdos puzzle").withDependency { weirdosDropDown }
    val weirdosColor: Color by ColorSetting("Weirdos Color", Color.GREEN, true, description = "Color for the weirdos solver").withDependency { weirdosSolver && weirdosDropDown }
    val weirdosStyle: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { weirdosSolver && weirdosDropDown }
    private val weirdosReset: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        WeirdosSolver.reset()
    }.withDependency { weirdosSolver && weirdosDropDown }

    private val quizDropdown: Boolean by DropdownSetting("Quiz")
    private val quizSolver: Boolean by BooleanSetting("Quiz Solver", false, description = "Solver for the trivia puzzle").withDependency { quizDropdown }
    val quizReset: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        QuizSolver.reset()
    }.withDependency { quizDropdown && quizSolver }


    init {
        execute(500) {
            if (tpMaze) TPMaze.scan()
            if (waterSolver) WaterSolver.scan()
            if (blazeSolver) BlazeSolver.getBlaze()
        }

        onPacket(S08PacketPlayerPosLook::class.java) {
            if (tpMaze) TPMaze.tpPacket(it)
        }

        onPacket(C08PacketPlayerBlockPlacement::class.java) {
            if (waterSolver) waterInteract(it)
        }

        onMessage(Regex("\\[NPC] (.+): (.+).?"), {enabled && weirdosSolver}) { str ->
            val (npc, message) = Regex("\\[NPC] (.+): (.+).?").find(str)?.destructured ?: return@onMessage
            WeirdosSolver.onNPCMessage(npc, message)
        }

        onWorldLoad {
            WaterSolver.reset()
            TPMaze.reset()
            TicTacToe.reset()
            IceFillSolver.reset()
            BlazeSolver.reset()
            BeamsSolver.reset()
            WeirdosSolver.reset()
            QuizSolver.reset()
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        profile("Puzzle Solvers") {
            if (waterSolver) WaterSolver.waterRender()
            if (tpMaze) TPMaze.tpRender()
            if (tttSolver) TTTSolver.tttRenderWorld()
            if (iceFillSolver) IceFillSolver.onRenderWorldLast(iceFillColor)
            if (blazeSolver) BlazeSolver.renderBlazes()
            if (beamsSolver) BeamsSolver.onRenderWorld()
            if (weirdosSolver) WeirdosSolver.onRenderWorld()
            if (quizSolver) QuizSolver.renderWorldLastQuiz()
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (tttSolver) TicTacToe.tttTick(event)
    }

    @SubscribeEvent
    fun onRoomEnter(event: EnteredDungeonRoomEvent) {
        IceFillSolver.enterDungeonRoom(event)
        BeamsSolver.enterDungeonRoom(event)
        TTTSolver.tttRoomEnter(event)
        QuizSolver.enterRoomQuiz(event)
    }

    @SubscribeEvent
    fun blockUpdateEvent(event: BlockChangeEvent) {
        BeamsSolver.onBlockChange(event)
    }

    @SubscribeEvent
    fun onMsg(event: ChatPacketEvent) {
        if (quizSolver) QuizSolver.onMessage(event.message)
    }
}
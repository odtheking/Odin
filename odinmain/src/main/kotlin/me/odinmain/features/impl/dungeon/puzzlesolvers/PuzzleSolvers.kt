package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.puzzlesolvers.WaterSolver.waterInteract
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.profile
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PuzzleSolvers : Module(
    name = "Puzzle Solvers",
    category = Category.DUNGEON,
    description = "Displays solutions for dungeon puzzles.",
    key = null
) {
    private val waterDropDown by DropdownSetting("Water Board")
    private val waterSolver by BooleanSetting("Water Board Solver", false, description = "Shows you the solution to the water puzzle.").withDependency { waterDropDown }
    val showOrder by BooleanSetting("Show Order", true, description = "Shows the order of the levers to click.").withDependency { waterSolver && waterDropDown }
    val showTracer by BooleanSetting("Show Tracer", true, description = "Shows a tracer to the next lever.").withDependency { waterSolver && waterDropDown }
    val tracerColorFirst by ColorSetting("Tracer Color First", Color.GREEN, true, description = "Color for the first tracer.").withDependency { showTracer && waterDropDown }
    val tracerColorSecond by ColorSetting("Tracer Color Second", Color.ORANGE, true, description = "Color for the second tracer.").withDependency { showTracer && waterDropDown }
    val reset by ActionSetting("Reset", description = "Resets the solver.") {
        WaterSolver.reset()
    }.withDependency { waterSolver && waterDropDown }

    private val mazeDropDown by DropdownSetting("TP Maze")
    private val tpMaze by BooleanSetting("Teleport Maze", false, description = "Shows you the solution for the TP maze puzzle.").withDependency { mazeDropDown }
    val mazeColorOne by ColorSetting("Color for one", Color.GREEN.withAlpha(.5f), true, description = "Color for when there is a single solution.").withDependency { tpMaze && mazeDropDown }
    val mazeColorMultiple by ColorSetting("Color for multiple", Color.ORANGE.withAlpha(.5f), true, description = "Color for when there are multiple solutions.").withDependency { tpMaze && mazeDropDown }
    val mazeColorVisited by ColorSetting("Color for visited", Color.RED.withAlpha(.5f), true, description = "Color for the already used TP pads.").withDependency { tpMaze && mazeDropDown }
    private val click by ActionSetting("Reset", description = "Resets the solver.") {
        TPMazeSolver.reset()
    }.withDependency { tpMaze && mazeDropDown }

    private val iceFillDropDown by DropdownSetting("Ice Fill")
    private val iceFillSolver by BooleanSetting("Ice Fill Solver", false, description = "Solver for the ice fill puzzle.").withDependency { iceFillDropDown }
    private val iceFillColor by ColorSetting("Ice Fill Color", Color.PINK, true, description = "Color for the ice fill solver.").withDependency { iceFillSolver && iceFillDropDown }
    val useOptimizedPatterns by BooleanSetting("Use Optimized Patterns", false, description = "Use optimized patterns for the ice fill solver.").withDependency { iceFillSolver && iceFillDropDown }
    private val action by ActionSetting("Reset", description = "Resets the solver.") {
        IceFillSolver.reset()
    }.withDependency { iceFillSolver && iceFillDropDown }

    private val blazeDropDown by DropdownSetting("Blaze")
    private val blazeSolver by BooleanSetting("Blaze Solver", description = "Shows you the solution for the Blaze puzzle").withDependency { blazeDropDown }
    val blazeLineNext by BooleanSetting("Blaze Solver Next Line", true, description = "Shows the next line to click.").withDependency { blazeSolver && blazeDropDown }
    val blazeLineAmount: Int by NumberSetting("Blaze Solver Lines", 1, 1, 10, 1, description = "Amount of lines to show.").withDependency { blazeSolver && blazeDropDown }
    val blazeStyle by SelectorSetting("Blaze Style", "Outline", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { blazeSolver && blazeDropDown }
    val blazeFirstColor by ColorSetting("First Color", Color.GREEN, true, description = "Color for the first blaze.").withDependency { blazeSolver && blazeDropDown }
    val blazeSecondColor by ColorSetting("Second Color", Color.ORANGE, true, description = "Color for the second blaze.").withDependency { blazeSolver && blazeDropDown }
    val blazeAllColor by ColorSetting("Other Color", Color.WHITE.withAlpha(.3f), true, description = "Color for the other blazes.").withDependency { blazeSolver && blazeDropDown }
    val blazeWidth by NumberSetting("Box Width", 1.0, 0.5, 2.0, 0.1, description = "Width of the box.").withDependency { blazeSolver && blazeDropDown }
    val blazeHeight by NumberSetting("Box Height", 2.0, 1.0, 3.0, 0.1, description = "Height of the box.").withDependency { blazeSolver && blazeDropDown }
    val blazeSendComplete by BooleanSetting("Send Complete", false, description = "Send complete message.").withDependency { blazeSolver && blazeDropDown }
    private val blazeReset by ActionSetting("Reset", description = "Resets the solver.") {
        BlazeSolver.reset()
    }.withDependency { blazeSolver && blazeDropDown }

    private val beamsDropDown by DropdownSetting("Creeper Beams")
    private val beamsSolver by BooleanSetting("Creeper Beams Solver", false, description = "Shows you the solution for the Creeper Beams puzzle.").withDependency { beamsDropDown }
    val beamStyle by SelectorSetting("Beam Style", "Filled Outline", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { beamsSolver && beamsDropDown }
    val beamsTracer by BooleanSetting("Tracer", false, description = "Shows a tracer to the next lantern.").withDependency { beamsSolver && beamsDropDown }
    val beamsAlpha by NumberSetting("Color Alpha", .7f, 0f, 1f, .05f, description = "The alpha of the color.").withDependency { beamsSolver && beamsDropDown }
    private val beamsReset by ActionSetting("Reset", description = "Resets the solver.") {
        BeamsSolver.reset()
    }.withDependency { beamsSolver && beamsDropDown }

    private val weirdosDropDown by DropdownSetting("Three Weirdos")
    private val weirdosSolver by BooleanSetting("Weirdos Solver", false, description = "Shows you the solution for the Weirdos puzzle.").withDependency { weirdosDropDown }
    val weirdosColor by ColorSetting("Weirdos Color", Color.GREEN.withAlpha(0.7f), true, description = "Color for the weirdos solver.").withDependency { weirdosSolver && weirdosDropDown }
    val weirdosWrongColor by ColorSetting("Weirdos Wrong Color", Color.RED.withAlpha(.7f), true,  description = "Color for the incorrect Weirdos.").withDependency { weirdosSolver && weirdosDropDown }
    val weirdosStyle by SelectorSetting("Weirdos Style", "Filled Outline", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.").withDependency { weirdosSolver && weirdosDropDown }
    private val weirdosReset by ActionSetting("Reset", description = "Resets the solver.") {
        WeirdosSolver.reset()
    }.withDependency { weirdosSolver && weirdosDropDown }

    private val quizDropdown by DropdownSetting("Quiz")
    private val quizSolver by BooleanSetting("Quiz Solver", false, description = "Solver for the trivia puzzle.").withDependency { quizDropdown }
    val quizColor by ColorSetting("Quiz Color", Color.GREEN.withAlpha(.75f), true, description = "Color for the quiz solver.").withDependency { quizDropdown && quizSolver }
    val quizDepth by BooleanSetting("Quiz Depth", false, description = "Depth check for the trivia puzzle.").withDependency { quizDropdown && quizSolver }
    val quizReset by ActionSetting("Reset", description = "Resets the solver.") {
        QuizSolver.reset()
    }.withDependency { quizDropdown && quizSolver }

    private val boulderDropDown by DropdownSetting("Boulder")
    private val boulderSolver by BooleanSetting("Boulder Solver", false, description = "Solver for the boulder puzzle.").withDependency { boulderDropDown }
    val showAllBoulderClicks by DualSetting("Boulder clicks", "Only First", "All Clicks", true, description = "Shows all the clicks or only the first.").withDependency { boulderDropDown && boulderSolver }
    val boulderStyle by SelectorSetting("Boulder Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { boulderDropDown && boulderSolver }
    val boulderColor by ColorSetting("Boulder Color", Color.GREEN.withAlpha(.5f), allowAlpha = true, description = "The color of the box.").withDependency { boulderDropDown && boulderSolver }
    val boulderLineWidth by NumberSetting("Boulder Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.").withDependency { boulderDropDown && boulderSolver }

    init {
        execute(500) {
            if (waterSolver) WaterSolver.scan()
            if (blazeSolver) BlazeSolver.getBlaze()
        }

        onPacket(S08PacketPlayerPosLook::class.java) {
            if (tpMaze) TPMazeSolver.tpPacket(it)
        }

        onPacket(C08PacketPlayerBlockPlacement::class.java) {
            if (waterSolver) waterInteract(it)
        }

        onMessage(Regex("\\[NPC] (.+): (.+).?"), { enabled && weirdosSolver }) { str ->
            val (npc, message) = Regex("\\[NPC] (.+): (.+).?").find(str)?.destructured ?: return@onMessage
            WeirdosSolver.onNPCMessage(npc, message)
        }

        onMessage(Regex(".*"), { enabled && quizSolver }) {
            QuizSolver.onMessage(it)
        }

        onWorldLoad {
            WaterSolver.reset()
            TPMazeSolver.reset()
            IceFillSolver.reset()
            BlazeSolver.reset()
            BeamsSolver.reset()
            WeirdosSolver.reset()
            QuizSolver.reset()
            BoulderSolver.reset()
            TTTSolver.reset()
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        profile("Puzzle Solvers") {
            if (waterSolver) WaterSolver.waterRender()
            if (tpMaze) TPMazeSolver.tpRender()
            //TTTSolver.tttRenderWorld()
            if (iceFillSolver) IceFillSolver.onRenderWorldLast(iceFillColor)
            if (blazeSolver) BlazeSolver.renderBlazes()
            if (beamsSolver) BeamsSolver.onRenderWorld()
            if (weirdosSolver) WeirdosSolver.onRenderWorld()
            if (quizSolver) QuizSolver.renderWorldLastQuiz()
            if (boulderSolver) BoulderSolver.onRenderWorld()
        }
    }

    @SubscribeEvent
    fun onMetaData(event: PostEntityMetadata) {
        //TTTSolver.onMetaData(event)
    }

    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        IceFillSolver.enterDungeonRoom(event)
        BeamsSolver.enterDungeonRoom(event)
        //TTTSolver.tttRoomEnter(event)
        QuizSolver.enterRoomQuiz(event)
        BoulderSolver.onRoomEnter(event)
        TPMazeSolver.onRoomEnter(event)
    }

    @SubscribeEvent
    fun blockUpdateEvent(event: BlockChangeEvent) {
        BeamsSolver.onBlockChange(event)
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        BoulderSolver.playerInteract(event)
    }
}
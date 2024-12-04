package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.RoomEnterEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.puzzlesolvers.WaterSolver.waterInteract
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.profile
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inBoss
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.dungeon.tiles.RoomType
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.block.BlockChest
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S24PacketBlockAction
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PuzzleSolvers : Module(
    name = "Puzzle Solvers",
    category = Category.DUNGEON,
    description = "Displays solutions for dungeon puzzles.",
    key = null
) {
    private val waterDropDown by DropdownSetting("Water Board")
    private val waterSolver by BooleanSetting("Water Board Solver", false, description = "Shows you the solution to the water puzzle.").withDependency { waterDropDown }
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
    private val mazeReset by ActionSetting("Reset", description = "Resets the solver.") {
        TPMazeSolver.reset()
    }.withDependency { tpMaze && mazeDropDown }

    private val iceFillDropDown by DropdownSetting("Ice Fill")
    private val iceFillSolver by BooleanSetting("Ice Fill Solver", false, description = "Solver for the ice fill puzzle.").withDependency { iceFillDropDown }
    private val iceFillColor by ColorSetting("Ice Fill Color", Color.PINK, true, description = "Color for the ice fill solver.").withDependency { iceFillSolver && iceFillDropDown }
    val useOptimizedPatterns by BooleanSetting("Use Optimized Patterns", false, description = "Use optimized patterns for the ice fill solver.").withDependency { iceFillSolver && iceFillDropDown }
    private val iceFillReset by ActionSetting("Reset", description = "Resets the solver.") {
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
    private val quizReset by ActionSetting("Reset", description = "Resets the solver.") {
        QuizSolver.reset()
    }.withDependency { quizDropdown && quizSolver }

    private val boulderDropDown by DropdownSetting("Boulder")
    private val boulderSolver by BooleanSetting("Boulder Solver", false, description = "Solver for the boulder puzzle.").withDependency { boulderDropDown }
    val showAllBoulderClicks by BooleanSetting("Show All Boulder Clicks", true, description = "Shows all the clicks or only the first.").withDependency { boulderDropDown && boulderSolver }
    val boulderStyle by SelectorSetting("Boulder Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { boulderDropDown && boulderSolver }
    val boulderColor by ColorSetting("Boulder Color", Color.GREEN.withAlpha(.5f), allowAlpha = true, description = "The color of the box.").withDependency { boulderDropDown && boulderSolver }
    val boulderLineWidth by NumberSetting("Boulder Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.").withDependency { boulderDropDown && boulderSolver }

    private val puzzleTimers by BooleanSetting("Puzzle Timers", true, description = "Shows the time it took to solve each puzzle.")
    private val sendPuzzleTime by BooleanSetting("Send Puzzle Time", false, description = "Sends the time it took to solve each puzzle in party chat.").withDependency { puzzleTimers }
    private val puzzleToIntMap = mapOf("Creeper Beams" to 0, "Lower Blaze" to 1, "Higher Blaze" to 2, "Boulder" to 3, "Ice Fill" to 4, "Quiz" to 5, "Teleport Maze" to 6, "Water Board" to 7, "Three Weirdos" to 8)
    private val puzzleTimersMap = hashMapOf<String, PuzzleTimer>()
    private data class PuzzleTimer(val timeEntered: Long = System.currentTimeMillis(), var sentMessage: Boolean = false)

    init {
        execute(500) {
            if ((!inDungeons || inBoss) && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) return@execute
            if (waterSolver) WaterSolver.scan()
            if (blazeSolver) BlazeSolver.getBlaze()
        }

        onPacket(S08PacketPlayerPosLook::class.java) {
            if ((!inDungeons || inBoss) && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) return@onPacket
            if (tpMaze) TPMazeSolver.tpPacket(it)
        }

        onPacket(C08PacketPlayerBlockPlacement::class.java) {
            if ((!inDungeons || inBoss) && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) return@onPacket
            if (waterSolver) waterInteract(it)
            if (boulderSolver) BoulderSolver.playerInteract(it)
        }

        onMessage(Regex("\\[NPC] (.+): (.+).?"), { enabled && weirdosSolver }) { str ->
            val (npc, message) = Regex("\\[NPC] (.+): (.+).?").find(str)?.destructured ?: return@onMessage
            WeirdosSolver.onNPCMessage(npc, message)
        }

        onMessage(Regex(".*"), { enabled && quizSolver }) {
            QuizSolver.onMessage(it)
        }

        onPacket(S24PacketBlockAction::class.java) {
            if ((!inDungeons || inBoss) && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) return@onPacket
            if (it.blockType !is BlockChest) return@onPacket
            val room = DungeonUtils.currentRoom?.takeIf { it.data.type == RoomType.PUZZLE } ?: return@onPacket

            when (room.data.name) {
                "Three Weirdos" -> it.blockPosition.equalsOneOf(room.getRealCoords(18, 69, 24), room.getRealCoords(16, 69, 25), room.getRealCoords(14, 69, 24))
                "Ice Fill"      -> it.blockPosition.equalsOneOf(room.getRealCoords(14, 75, 29), room.getRealCoords(16, 75, 29))
                "Teleport Maze" -> it.blockPosition == room.getRealCoords(15, 70, 20)
                "Water Board"   -> it.blockPosition == room.getRealCoords(15, 56, 22)
                "Boulder"       -> it.blockPosition == room.getRealCoords(15, 66, 29)
                else            -> false
            }.takeIf { !it } ?: onPuzzleComplete(room.data.name)
        }

        onWorldLoad {
            puzzleTimersMap.clear()
            IceFillSolver.reset()
            WeirdosSolver.reset()
            BoulderSolver.reset()
            TPMazeSolver.reset()
            WaterSolver.reset()
            BlazeSolver.reset()
            BeamsSolver.reset()
            QuizSolver.reset()
            TTTSolver.reset()
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if ((!inDungeons || inBoss) && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) return
        profile("Puzzle Solvers Render") {
            if (waterSolver) WaterSolver.onRenderWorld()
            if (tpMaze) TPMazeSolver.onRenderWorld()
            if (iceFillSolver) IceFillSolver.onRenderWorld(iceFillColor)
            if (blazeSolver) BlazeSolver.onRenderWorld()
            if (beamsSolver) BeamsSolver.onRenderWorld()
            if (weirdosSolver) WeirdosSolver.onRenderWorld()
            if (quizSolver) QuizSolver.onRenderWorld()
            if (boulderSolver) BoulderSolver.onRenderWorld()
        }
    }

    private val puzzlePBs = PersonalBest("Puzzles", 9)

    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        IceFillSolver.onRoomEnter(event)
        BeamsSolver.onRoomEnter(event)
        QuizSolver.onRoomEnter(event)
        BoulderSolver.onRoomEnter(event)
        TPMazeSolver.onRoomEnter(event)
        if (puzzleTimers && event.room?.data?.type == RoomType.PUZZLE && puzzleTimersMap.none { it.key == event.room.data.name }) puzzleTimersMap[event.room.data.name] = PuzzleTimer()
    }

    @SubscribeEvent
    fun blockUpdateEvent(event: BlockChangeEvent) {
        if ((!inDungeons || inBoss) && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) return
        if (beamsSolver) BeamsSolver.onBlockChange(event)
    }

    fun onPuzzleComplete(puzzleName: String) {
        puzzleTimersMap[puzzleName]?.let {
            if (it.sentMessage) return
            puzzlePBs.time(puzzleToIntMap[puzzleName] ?: return@let, (System.currentTimeMillis() - it.timeEntered) / 1000.0, "s§7!", "§a${puzzleName} §7solved in §6", addPBString = true, addOldPBString = true, sendOnlyPB = false)
            if (sendPuzzleTime) partyMessage("It took me ${(System.currentTimeMillis() - it.timeEntered) / 1000.0} seconds to solve the $puzzleName puzzle. ${if ((System.currentTimeMillis() - it.timeEntered) / 1000.0 > 30) ":(" else ":)"}")
            it.sentMessage = true
        }
    }
}
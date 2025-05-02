package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.RoomEnterEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.puzzlesolvers.WaterSolver.waterInteract
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.profile
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PersonalBest
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inBoss
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.dungeon.tiles.RoomType
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.block.BlockChest
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S24PacketBlockAction
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PuzzleSolvers : Module(
    name = "Puzzle Solvers",
    desc = "Displays solutions for Water Board, TP Maze, Ice Fill, Blaze, Creeper Beams, Three Weirdos, Quiz and Boulder dungeon puzzles.",
    key = null
) {
    private val waterDropDown by DropdownSetting("Water Board")
    private val waterSolver by BooleanSetting("Water Board Solver", false, desc = "Shows you the solution to the water puzzle.").withDependency { waterDropDown }
    private val optimizedSolutions by BooleanSetting("Optimized Solutions", false, desc = "Use optimized solutions for the water puzzle.").withDependency { waterSolver && waterDropDown }
    private val showTracer by BooleanSetting("Show Tracer", true, desc = "Shows a tracer to the next lever.").withDependency { waterSolver && waterDropDown }
    private val tracerColorFirst by ColorSetting("Tracer Color First", Colors.MINECRAFT_GREEN, true, desc = "Color for the first tracer.").withDependency { showTracer && waterDropDown }
    private val tracerColorSecond by ColorSetting("Tracer Color Second", Colors.MINECRAFT_GOLD, true, desc = "Color for the second tracer.").withDependency { showTracer && waterDropDown }
    private val waterReset by ActionSetting("Reset", desc = "Resets the solver.") {
        WaterSolver.reset()
    }.withDependency { waterSolver && waterDropDown }

    private val mazeDropDown by DropdownSetting("TP Maze")
    private val tpMaze by BooleanSetting("TP Maze Solver", false, desc = "Shows you the solution for the TP maze puzzle.").withDependency { mazeDropDown }
    private val mazeColorOne by ColorSetting("Color for one", Colors.MINECRAFT_GREEN.withAlpha(.5f), true, desc = "Color for when there is a single solution.").withDependency { tpMaze && mazeDropDown }
    private val mazeColorMultiple by ColorSetting("Color for multiple", Colors.MINECRAFT_GOLD.withAlpha(.5f), true, desc = "Color for when there are multiple solutions.").withDependency { tpMaze && mazeDropDown }
    private val mazeColorVisited by ColorSetting("Color for visited", Colors.MINECRAFT_RED.withAlpha(.5f), true, desc = "Color for the already used TP pads.").withDependency { tpMaze && mazeDropDown }
    private val mazeReset by ActionSetting("Reset", desc = "Resets the solver.") {
        TPMazeSolver.reset()
    }.withDependency { tpMaze && mazeDropDown }

    private val iceFillDropDown by DropdownSetting("Ice Fill")
    private val iceFillSolver by BooleanSetting("Ice Fill Solver", false, desc = "Solver for the ice fill puzzle.").withDependency { iceFillDropDown }
    private val iceFillColor by ColorSetting("Ice Fill Color", Colors.MINECRAFT_LIGHT_PURPLE, true, desc = "Color for the ice fill solver.").withDependency { iceFillSolver && iceFillDropDown }
    private val useOptimizedPatterns by BooleanSetting("Use Optimized Patterns", false, desc = "Use optimized patterns for the ice fill solver.").withDependency { iceFillSolver && iceFillDropDown }
    private val iceFillReset by ActionSetting("Reset", desc = "Resets the solver.") {
        IceFillSolver.reset()
    }.withDependency { iceFillSolver && iceFillDropDown }

    private val blazeDropDown by DropdownSetting("Blaze")
    private val blazeSolver by BooleanSetting("Blaze Solver", desc = "Shows you the solution for the Blaze puzzle").withDependency { blazeDropDown }
    private val blazeLineNext by BooleanSetting("Blaze Solver Next Line", true, desc = "Shows the next line to click.").withDependency { blazeSolver && blazeDropDown }
    private val blazeLineAmount by NumberSetting("Blaze Solver Lines", 1, 1, 10, 1, desc = "Amount of lines to show.").withDependency { blazeSolver && blazeDropDown }
    private val blazeLineWidth by NumberSetting("Blaze Solver Lines Width", 2f, 0.5, 5, 0.1, desc = "Width for blaze lines.").withDependency { blazeSolver && blazeDropDown }
    private val blazeStyle by SelectorSetting("Blaze Style", "Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "Whether or not the box should be filled.").withDependency { blazeSolver && blazeDropDown }
    private val blazeFirstColor by ColorSetting("First Color", Colors.MINECRAFT_GREEN.withAlpha(.75f), true, desc = "Color for the first blaze.").withDependency { blazeSolver && blazeDropDown }
    private val blazeSecondColor by ColorSetting("Second Color", Colors.MINECRAFT_GOLD.withAlpha(.75f), true, desc = "Color for the second blaze.").withDependency { blazeSolver && blazeDropDown }
    private val blazeAllColor by ColorSetting("Other Color", Colors.WHITE.withAlpha(.3f), true, desc = "Color for the other blazes.").withDependency { blazeSolver && blazeDropDown }
    private val blazeWidth by NumberSetting("Box Width", 1f, 0.5, 2.0, 0.1, desc = "Width of the box.").withDependency { blazeSolver && blazeDropDown }
    private val blazeHeight by NumberSetting("Box Height", 2f, 1.0, 3.0, 0.1, desc = "Height of the box.").withDependency { blazeSolver && blazeDropDown }
    private val blazeSendComplete by BooleanSetting("Send Complete", false, desc = "Send complete message.").withDependency { blazeSolver && blazeDropDown }
    private val blazeReset by ActionSetting("Reset", desc = "Resets the solver.") {
        BlazeSolver.reset()
    }.withDependency { blazeSolver && blazeDropDown }

    private val beamsDropDown by DropdownSetting("Creeper Beams")
    private val beamsSolver by BooleanSetting("Creeper Beams Solver", false, desc = "Shows you the solution for the Creeper Beams puzzle.").withDependency { beamsDropDown }
    private val beamStyle by SelectorSetting("Beams Style", "Filled Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "Whether or not the box should be filled.").withDependency { beamsSolver && beamsDropDown }
    private val beamsTracer by BooleanSetting("Beams Tracer", false, desc = "Shows a tracer to the next lantern.").withDependency { beamsSolver && beamsDropDown }
    private val beamsAlpha by NumberSetting("Beams Color Alpha", .7f, 0f, 1f, .05f, desc = "The alpha of the color.").withDependency { beamsSolver && beamsDropDown }
    private val beamsReset by ActionSetting("Reset", desc = "Resets the solver.") {
        BeamsSolver.reset()
    }.withDependency { beamsSolver && beamsDropDown }

    private val weirdosDropDown by DropdownSetting("Three Weirdos")
    private val weirdosSolver by BooleanSetting("Weirdos Solver", false, desc = "Shows you the solution for the Weirdos puzzle.").withDependency { weirdosDropDown }
    private val weirdosColor by ColorSetting("Weirdos Correct Color", Colors.MINECRAFT_GREEN.withAlpha(0.7f), true, desc = "Color for the weirdos solver.").withDependency { weirdosSolver && weirdosDropDown }
    private val weirdosWrongColor by ColorSetting("Weirdos Wrong Color", Colors.MINECRAFT_RED.withAlpha(.7f), true,  desc = "Color for the incorrect Weirdos.").withDependency { weirdosSolver && weirdosDropDown }
    private val weirdosStyle by SelectorSetting("Weirdos Style", "Filled Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "Whether or not the box should be filled.").withDependency { weirdosSolver && weirdosDropDown }
    private val weirdosReset by ActionSetting("Reset", desc = "Resets the solver.") {
        WeirdosSolver.reset()
    }.withDependency { weirdosSolver && weirdosDropDown }

    private val quizDropdown by DropdownSetting("Quiz")
    private val quizSolver by BooleanSetting("Quiz Solver", false, desc = "Solver for the trivia puzzle.").withDependency { quizDropdown }
    private val quizColor by ColorSetting("Quiz Color", Colors.MINECRAFT_GREEN.withAlpha(.75f), true, desc = "Color for the quiz solver.").withDependency { quizDropdown && quizSolver }
    private val quizDepth by BooleanSetting("Quiz Depth", false, desc = "Depth check for the trivia puzzle.").withDependency { quizDropdown && quizSolver }
    private val quizReset by ActionSetting("Reset", desc = "Resets the solver.") {
        QuizSolver.reset()
    }.withDependency { quizDropdown && quizSolver }

    private val boulderDropDown by DropdownSetting("Boulder")
    private val boulderSolver by BooleanSetting("Boulder Solver", false, desc = "Solver for the boulder puzzle.").withDependency { boulderDropDown }
    private val showAllBoulderClicks by BooleanSetting("Show All Boulder Clicks", true, desc = "Shows all the clicks or only the first.").withDependency { boulderDropDown && boulderSolver }
    private val boulderStyle by SelectorSetting("Boulder Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { boulderDropDown && boulderSolver }
    private val boulderColor by ColorSetting("Boulder Color", Colors.MINECRAFT_GREEN.withAlpha(.5f), allowAlpha = true, desc = "The color of the box.").withDependency { boulderDropDown && boulderSolver }
    private val boulderLineWidth by NumberSetting("Boulder Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.").withDependency { boulderDropDown && boulderSolver }

    private val puzzleTimers by BooleanSetting("Puzzle Timers", true, desc = "Shows the time it took to solve each puzzle.")
    private val sendPuzzleTime by BooleanSetting("Send Puzzle Time", false, desc = "Sends the time it took to solve each puzzle in party chat.").withDependency { puzzleTimers }
    private val puzzleToIntMap = mapOf("Creeper Beams" to 0, "Lower Blaze" to 1, "Higher Blaze" to 2, "Boulder" to 3, "Ice Fill" to 4, "Quiz" to 5, "Teleport Maze" to 6, "Water Board" to 7, "Three Weirdos" to 8)
    private val puzzleTimersMap = hashMapOf<String, PuzzleTimer>()
    private data class PuzzleTimer(val timeEntered: Long = System.currentTimeMillis(), var sentMessage: Boolean = false)

    init {
        execute(500) {
            if (!inDungeons || inBoss) return@execute
            if (blazeSolver) BlazeSolver.getBlaze()
            if (waterSolver) WaterSolver.scan(optimizedSolutions)
        }

        onPacket<S08PacketPlayerPosLook> {
            if (!inDungeons || inBoss) return@onPacket
            if (tpMaze) TPMazeSolver.tpPacket(it)
        }

        onPacket<C08PacketPlayerBlockPlacement> {
            if (!inDungeons || inBoss) return@onPacket
            if (waterSolver) waterInteract(it)
            if (boulderSolver) BoulderSolver.playerInteract(it)
        }

        onMessage(Regex("\\[NPC] (.+): (.+).?"), { enabled && weirdosSolver && inDungeons && !inBoss }) { str ->
            val (npc, message) = str.destructured
            WeirdosSolver.onNPCMessage(npc, message)
        }

        onMessage(Regex(".*"), { enabled && quizSolver && inDungeons && !inBoss }) {
            QuizSolver.onMessage(it.value)
        }

        onPacket<S32PacketConfirmTransaction> {
            if (!inDungeons || inBoss) return@onPacket
            if (waterSolver) WaterSolver.onServerTick()
        }

        onPacket<S24PacketBlockAction> { packet ->
            if (!inDungeons || inBoss || packet.blockType !is BlockChest) return@onPacket
            val room = DungeonUtils.currentRoom?.takeIf { room -> room.data.type == RoomType.PUZZLE } ?: return@onPacket

            when (room.data.name) {
                "Three Weirdos" -> packet.blockPosition.equalsOneOf(room.getRealCoords(18, 69, 24), room.getRealCoords(16, 69, 25), room.getRealCoords(14, 69, 24))
                "Ice Fill"      -> packet.blockPosition.equalsOneOf(room.getRealCoords(14, 75, 29), room.getRealCoords(16, 75, 29))
                "Teleport Maze" -> packet.blockPosition == room.getRealCoords(15, 70, 20)
                "Water Board"   -> packet.blockPosition == room.getRealCoords(15, 56, 22)
                "Boulder"       -> packet.blockPosition == room.getRealCoords(15, 66, 29)
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
        if (!inDungeons || inBoss) return
        profile("Puzzle Solvers Render") {
            if (iceFillSolver) IceFillSolver.onRenderWorld(iceFillColor)
            if (weirdosSolver) WeirdosSolver.onRenderWorld(weirdosColor, weirdosWrongColor, weirdosStyle)
            if (boulderSolver) BoulderSolver.onRenderWorld(showAllBoulderClicks, boulderStyle, boulderColor, boulderLineWidth)
            if (blazeSolver)   BlazeSolver.onRenderWorld(blazeLineNext, blazeLineAmount, blazeStyle, blazeFirstColor, blazeSecondColor, blazeAllColor, blazeWidth, blazeHeight, blazeSendComplete, blazeLineWidth)
            if (beamsSolver)   BeamsSolver.onRenderWorld(beamStyle, beamsTracer, beamsAlpha)
            if (waterSolver)   WaterSolver.onRenderWorld(showTracer, tracerColorFirst, tracerColorSecond)
            if (quizSolver)    QuizSolver.onRenderWorld(quizColor, quizDepth)
            if (tpMaze)        TPMazeSolver.onRenderWorld(mazeColorOne, mazeColorMultiple, mazeColorVisited)
        }
    }

    private val puzzlePBs = PersonalBest("Puzzles", 9)

    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        BoulderSolver.onRoomEnter(event)
        IceFillSolver.onRoomEnter(event, useOptimizedPatterns)
        TPMazeSolver.onRoomEnter(event)
        BeamsSolver.onRoomEnter(event)
        QuizSolver.onRoomEnter(event)
        if (puzzleTimers && event.room?.data?.type == RoomType.PUZZLE && puzzleTimersMap.none { it.key == event.room.data.name }) puzzleTimersMap[event.room.data.name] = PuzzleTimer()
    }

    @SubscribeEvent
    fun blockUpdateEvent(event: BlockChangeEvent) {
        if (!inDungeons || inBoss) return
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
package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomType
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.block.Blocks

object PuzzleSolvers : Module(
    name = "Puzzle Solvers",
    description = "Displays solutions for Water Board, TP Maze, Ice Fill, Blaze, Creeper Beams, Three Weirdos, Quiz and Boulder dungeon puzzles.",
    key = null
) {
    private val waterDropDown by DropdownSetting("Water Board")
    private val waterSolver by BooleanSetting("Water Board Solver", true, desc = "Shows you the solution to the water puzzle.").withDependency { waterDropDown }
    private val optimizedSolutions by BooleanSetting("Optimized Solutions", false, desc = "Use optimized solutions for the water puzzle.").withDependency { waterSolver && waterDropDown }
    private val showTracer by BooleanSetting("Maze Tracer", true, desc = "Shows a tracer to the next lever.").withDependency { waterSolver && waterDropDown }
    private val tracerColorFirst by ColorSetting("Tracer Color First", Colors.MINECRAFT_GREEN, true, desc = "Color for the first tracer.").withDependency { showTracer && waterDropDown }
    private val tracerColorSecond by ColorSetting("Tracer Color Second", Colors.MINECRAFT_GOLD, true, desc = "Color for the second tracer.").withDependency { showTracer && waterDropDown }
    private val waterReset by ActionSetting("Reset Water", desc = "Resets the solver.") { WaterSolver.reset() }.withDependency { waterSolver && waterDropDown }

    private val mazeDropDown by DropdownSetting("TP Maze")
    private val tpMaze by BooleanSetting("TP Maze Solver", true, desc = "Shows you the solution for the TP maze puzzle.").withDependency { mazeDropDown }
    private val mazeColorOne by ColorSetting("Color for one", Colors.MINECRAFT_GREEN.withAlpha(.5f), true, desc = "Color for when there is a single solution.").withDependency { tpMaze && mazeDropDown }
    private val mazeColorMultiple by ColorSetting("Color for multiple", Colors.MINECRAFT_GOLD.withAlpha(.5f), true, desc = "Color for when there are multiple solutions.").withDependency { tpMaze && mazeDropDown }
    private val mazeColorVisited by ColorSetting("Color for visited", Colors.MINECRAFT_RED.withAlpha(.5f), true, desc = "Color for the already used TP pads.").withDependency { tpMaze && mazeDropDown }
    private val mazeShowTracer by BooleanSetting("Maze Tracer", true, desc = "Shows a tracer to the best next TP pad.").withDependency { tpMaze && mazeDropDown }
    private val mazeTracerColor by ColorSetting("Tracer Color", Colors.MINECRAFT_AQUA, true, desc = "Color for the TP maze tracer.").withDependency { mazeShowTracer && tpMaze && mazeDropDown }
    private val mazeReset by ActionSetting("Reset TP Maze", desc = "Resets the solver.") { TPMazeSolver.reset() }.withDependency { tpMaze && mazeDropDown }

    private val iceFillDropDown by DropdownSetting("Ice Fill")
    private val iceFillSolver by BooleanSetting("Ice Fill Solver", true, desc = "Solver for the ice fill puzzle.").withDependency { iceFillDropDown }
    private val iceFillColor by ColorSetting("Ice Fill Color", Colors.MINECRAFT_LIGHT_PURPLE, true, desc = "Color for the ice fill solver.").withDependency { iceFillSolver && iceFillDropDown }
    private val useOptimizedPatterns by BooleanSetting("Use Optimized Patterns", false, desc = "Use optimized patterns for the ice fill solver.").withDependency { iceFillSolver && iceFillDropDown }
    private val iceFillReset by ActionSetting("Reset Ice Fill", desc = "Resets the solver.") { IceFillSolver.reset() }.withDependency { iceFillSolver && iceFillDropDown }

    private val blazeDropDown by DropdownSetting("Blaze")
    private val blazeSolver by BooleanSetting("Blaze Solver", true, desc = "Shows you the solution for the Blaze puzzle").withDependency { blazeDropDown }
    private val blazeLineNext by BooleanSetting("Blaze Solver Next Line", true, desc = "Shows the next line to click.").withDependency { blazeSolver && blazeDropDown }
    private val blazeLineAmount by NumberSetting("Blaze Solver Lines", 1, 1, 10, 1, desc = "Amount of lines to show.").withDependency { blazeSolver && blazeDropDown }
    private val blazeLineWidth by NumberSetting("Blaze Solver Lines Width", 2f, 0.5, 5, 0.1, desc = "Width for blaze lines.").withDependency { blazeSolver && blazeDropDown }
    private val blazeStyle by SelectorSetting("Blaze Style", "Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "Whether or not the box should be filled.").withDependency { blazeSolver && blazeDropDown }
    private val blazeFirstColor by ColorSetting("First Color", Colors.MINECRAFT_GREEN.withAlpha(.75f), true, desc = "Color for the first blaze.").withDependency { blazeSolver && blazeDropDown }
    private val blazeSecondColor by ColorSetting("Second Color", Colors.MINECRAFT_GOLD.withAlpha(.75f), true, desc = "Color for the second blaze.").withDependency { blazeSolver && blazeDropDown }
    private val blazeThirdColor by ColorSetting("Third Color", Colors.MINECRAFT_RED.withAlpha(.75f), true, desc = "Color for the third blaze.").withDependency { blazeSolver && blazeDropDown }
    private val blazeAllColor by ColorSetting("Other Color", Colors.WHITE.withAlpha(.3f), true, desc = "Color for the other blazes.").withDependency { blazeSolver && blazeDropDown }
    private val blazeSendComplete by BooleanSetting("Send Complete", false, desc = "Send complete message.").withDependency { blazeSolver && blazeDropDown }
    private val blazeReset by ActionSetting("Reset Blaze", desc = "Resets the solver.") { BlazeSolver.reset() }.withDependency { blazeSolver && blazeDropDown }

    private val beamsDropDown by DropdownSetting("Creeper Beams")
    private val beamsSolver by BooleanSetting("Creeper Beams Solver", true, desc = "Shows you the solution for the Creeper Beams puzzle.").withDependency { beamsDropDown }
    private val beamStyle by SelectorSetting("Beams Style", "Filled Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "Whether or not the box should be filled.").withDependency { beamsSolver && beamsDropDown }
    private val beamsTracer by BooleanSetting("Beams Tracer", false, desc = "Shows a tracer to the next lantern.").withDependency { beamsSolver && beamsDropDown }
    private val beamsAlpha by NumberSetting("Beams Color Alpha", .7f, 0f, 1f, .05f, desc = "The alpha of the color.").withDependency { beamsSolver && beamsDropDown }
    private val beamsReset by ActionSetting("Reset Beams", desc = "Resets the solver.") { BeamsSolver.reset() }.withDependency { beamsSolver && beamsDropDown }

    private val weirdosDropDown by DropdownSetting("Three Weirdos")
    private val weirdosSolver by BooleanSetting("Weirdos Solver", true, desc = "Shows you the solution for the Weirdos puzzle.").withDependency { weirdosDropDown }
    private val weirdosColor by ColorSetting("Weirdos Correct Color", Colors.MINECRAFT_GREEN.withAlpha(0.7f), true, desc = "Color for the weirdos solver.").withDependency { weirdosSolver && weirdosDropDown }
    private val weirdosWrongColor by ColorSetting("Weirdos Wrong Color", Colors.MINECRAFT_RED.withAlpha(.7f), true,  desc = "Color for the incorrect Weirdos.").withDependency { weirdosSolver && weirdosDropDown }
    private val weirdosStyle by SelectorSetting("Weirdos Style", "Filled Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "Whether or not the box should be filled.").withDependency { weirdosSolver && weirdosDropDown }
    private val weirdosReset by ActionSetting("Reset Weirdos", desc = "Resets the solver.") { WeirdosSolver.reset() }.withDependency { weirdosSolver && weirdosDropDown }

    private val quizDropdown by DropdownSetting("Quiz")
    private val quizSolver by BooleanSetting("Quiz Solver", true, desc = "Solver for the trivia puzzle.").withDependency { quizDropdown }
    private val quizColor by ColorSetting("Quiz Color", Colors.MINECRAFT_GREEN.withAlpha(.75f), true, desc = "Color for the quiz solver.").withDependency { quizDropdown && quizSolver }
    private val quizDepth by BooleanSetting("Quiz Depth", false, desc = "Depth check for the trivia puzzle.").withDependency { quizDropdown && quizSolver }
    private val quizReset by ActionSetting("Reset Quiz", desc = "Resets the solver.") { QuizSolver.reset() }.withDependency { quizDropdown && quizSolver }

    private val boulderDropDown by DropdownSetting("Boulder")
    private val boulderSolver by BooleanSetting("Boulder Solver", true, desc = "Solver for the boulder puzzle.").withDependency { boulderDropDown }
    private val showAllBoulderClicks by BooleanSetting("Show All Boulder Clicks", false, desc = "Shows all the clicks or only the first.").withDependency { boulderDropDown && boulderSolver }
    private val boulderStyle by SelectorSetting("Boulder Style", "Outline", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "Whether or not the box should be filled.").withDependency { boulderDropDown && boulderSolver }
    private val boulderColor by ColorSetting("Boulder Color", Colors.MINECRAFT_GREEN.withAlpha(.5f), true, desc = "The color of the box.").withDependency { boulderDropDown && boulderSolver }

    private val puzzleTimers by BooleanSetting("Puzzle Timers", true, desc = "Shows the time it took to solve each puzzle.")
    private val draftPrompt by BooleanSetting("Draft prompt", true, desc = "Automatically gets architect's draft when failing a puzzle room.")
    private val failRegex = Regex("^PUZZLE FAIL! (\\w{1,16}) .+$|^\\[STATUE] Oruo the Omniscient: (\\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\\.$")
    private val puzzleTimersMap = hashMapOf<String, PuzzleTimer>()
    private data class PuzzleTimer(val timeEntered: Long = System.currentTimeMillis(), var sentMessage: Boolean = false)
    private val weirdosRegex = Regex("\\[NPC] (.+): (.+).?")

    private inline val isInPuzzle get() = DungeonUtils.currentRoom?.data?.type == RoomType.PUZZLE

    init {
        TickTask(10) {
            if (!enabled || !isInPuzzle) return@TickTask
            if (blazeSolver) BlazeSolver.getBlaze()
            if (waterSolver) WaterSolver.scan(optimizedSolutions)
        }

        on<TickEvent.Server> {
            if (!DungeonUtils.inClear) return@on
            if (waterSolver) WaterSolver.onServerTick()
        }

        on<WorldEvent.Load> {
            puzzleTimersMap.clear()
            IceFillSolver.reset()
            WeirdosSolver.reset()
            BoulderSolver.reset()
            TPMazeSolver.reset()
            WaterSolver.reset()
            BlazeSolver.reset()
            BeamsSolver.reset()
            QuizSolver.reset()
        }

        on<RoomEnterEvent> {
            BoulderSolver.onRoomEnter(this)
            IceFillSolver.onRoomEnter(this, useOptimizedPatterns)
            TPMazeSolver.onRoomEnter(this)
            BeamsSolver.onRoomEnter(this)
            QuizSolver.onRoomEnter(this)
            if (puzzleTimers && this.room?.data?.type == RoomType.PUZZLE && puzzleTimersMap.none { it.key == this.room.data.name }) puzzleTimersMap[this.room.data.name] = PuzzleTimer()
        }

        on<BlockUpdateEvent> {
            if (!DungeonUtils.inClear) return@on
            if (beamsSolver) BeamsSolver.onBlockChange(this)
        }

        onReceive<ClientboundPlayerPositionPacket> {
            if (!isInPuzzle) return@onReceive
            if (tpMaze) TPMazeSolver.tpPacket(this)
        }

        on<ChatPacketEvent> {
            if (!DungeonUtils.inClear) return@on
            if (draftPrompt && isInPuzzle) failRegex.find(value)?.destructured?.let {
                modMessage("§7Click §ehere §7to fetch architect's draft", chatStyle = Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand("gfs architect's first draft 1"))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("Click to fetch the architect's draft"))))
            }
            if (weirdosSolver) weirdosRegex.find(value)?.destructured?.let { (npc, message) -> WeirdosSolver.onNPCMessage(npc, message) }
            if (quizSolver) QuizSolver.onMessage(value)
        }

        onReceive<ClientboundBlockEventPacket> {
            if (!DungeonUtils.inClear || block != Blocks.CHERRY_LOG) return@onReceive
            val room = DungeonUtils.currentRoom?.takeIf { room -> room.data.type == RoomType.PUZZLE } ?: return@onReceive

            when (room.data.name) {
                "Three Weirdos" -> pos.equalsOneOf(room.getRealCoords(BlockPos(18, 69, 24)), room.getRealCoords(BlockPos(16, 69, 25)), room.getRealCoords(BlockPos(14, 69, 24)))
                "Ice Fill"      -> pos.equalsOneOf(room.getRealCoords(BlockPos(14, 75, 29)), room.getRealCoords(BlockPos(16, 75, 29)))
                "Teleport Maze" -> pos == room.getRealCoords(BlockPos(15, 70, 20))
                "Water Board"   -> pos == room.getRealCoords(BlockPos(15, 56, 22))
                "Boulder"       -> pos == room.getRealCoords(BlockPos(15, 66, 29))
                else            -> false
            }.takeIf { !it } ?: onPuzzleComplete(room.data.name)
        }

        onSend<ServerboundUseItemOnPacket> {
            if (!DungeonUtils.inClear || this.hand == InteractionHand.OFF_HAND) return@onSend
            if (waterSolver) WaterSolver.waterInteract(this)
            if (boulderSolver) BoulderSolver.playerInteract(this)
       }

        on<RenderEvent.Extract> {
            if (!DungeonUtils.inClear) return@on
            if (iceFillSolver) IceFillSolver.onRenderWorld(this, iceFillColor)
            if (weirdosSolver) WeirdosSolver.onRenderWorld(this, weirdosColor, weirdosWrongColor, weirdosStyle)
            if (boulderSolver) BoulderSolver.onRenderWorld(this, showAllBoulderClicks, boulderStyle, boulderColor)
            if (blazeSolver)   BlazeSolver.onRenderWorld(this, blazeLineNext, blazeLineAmount, blazeStyle, blazeFirstColor, blazeSecondColor, blazeThirdColor, blazeAllColor, blazeSendComplete, blazeLineWidth)
            if (beamsSolver)   BeamsSolver.onRenderWorld(this, beamStyle, beamsTracer, beamsAlpha)
            if (waterSolver)   WaterSolver.onRenderWorld(this, showTracer, tracerColorFirst, tracerColorSecond)
            if (quizSolver)    QuizSolver.onRenderWorld(this, quizColor, quizDepth)
            if (tpMaze)        TPMazeSolver.onRenderWorld(this, mazeColorOne, mazeColorMultiple, mazeColorVisited, mazeShowTracer, mazeTracerColor)
        }
    }

    private val puzzlePBs = PersonalBest(this, "PuzzlePBs")

    fun onPuzzleComplete(puzzleName: String) {
        puzzleTimersMap[puzzleName]?.let {
            if (it.sentMessage) return
            puzzlePBs.time(puzzleName, (System.currentTimeMillis() - it.timeEntered) / 1000f, "s§7!", "§a${puzzleName} §7solved in §6")
            it.sentMessage = true
        }
    }
}
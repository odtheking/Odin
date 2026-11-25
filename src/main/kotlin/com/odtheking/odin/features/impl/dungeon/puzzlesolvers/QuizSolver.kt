package com.odtheking.odin.features.impl.dungeon.puzzlesolvers

import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.onPuzzleComplete
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.JsonResourceLoader
import com.odtheking.odin.utils.handlers.ServerTick
import com.odtheking.odin.utils.render.drawBeaconBeam
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.startsWithOneOf
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.AABB

object QuizSolver {
    private const val START_DURATION = 220
    private const val QUESTION_DURATION = 100

    private var answers: MutableMap<String, List<String>> = JsonResourceLoader.loadJson(Identifier.fromNamespaceAndPath("odin", "puzzles/quiz-answers.json")) ?: mutableMapOf()
    private var triviaAnswers: List<String>? = null

    private var triviaOptions: MutableList<TriviaAnswer> = MutableList(3) { TriviaAnswer(null, false) }
    private data class TriviaAnswer(var blockPos: BlockPos?, var isCorrect: Boolean)

    private var timerEndTick = 0L
    private var timerMax = 0
    private var timerStage = 0
    val timer: Triple<Int, Int, Int> get() = Triple((timerEndTick - ServerTick.now).coerceAtLeast(0).toInt(), timerMax, timerStage)

    private fun startTimer(duration: Int, stage: Int) {
        timerEndTick = ServerTick.now + duration
        timerMax = duration
        timerStage = stage
    }

    fun onMessage(msg: String) {
        when (msg) {
            "[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know." -> startTimer(START_DURATION, 1)
            "[STATUE] Oruo the Omniscient: 2 questions left... Then you will have proven your worth to me!" -> startTimer(QUESTION_DURATION, 2)
            "[STATUE] Oruo the Omniscient: One more question!" -> startTimer(QUESTION_DURATION, 3)
        }

        if (msg.startsWith("[STATUE] Oruo the Omniscient: ") && msg.endsWith("correctly!")) {
            if (msg.contains("answered the final question")) {
                onPuzzleComplete("Quiz")
                reset()
                return
            }
            if (msg.contains("answered Question #")) triviaOptions.forEach { it.isCorrect = false }
        }

        if (msg.trim().startsWithOneOf("ⓐ", "ⓑ", "ⓒ", ignoreCase = true) && triviaAnswers?.any { msg.endsWith(it) } == true) {
            when (msg.trim()[0]) {
                'ⓐ' -> triviaOptions[0].isCorrect = true
                'ⓑ' -> triviaOptions[1].isCorrect = true
                'ⓒ' -> triviaOptions[2].isCorrect = true
            }
        }

        triviaAnswers = when {
            msg.trim() == "What SkyBlock year is it?" -> listOf("Year ${(((System.currentTimeMillis() / 1000) - 1560276000) / 446400).toInt() + 1}")
            else -> answers.entries.find { msg.contains(it.key) }?.value ?: return
        }
    }

    fun onRoomEnter(event: RoomEnterEvent) = with(event.room) {
        if (this?.data?.name != "Quiz") return@with

        triviaOptions[0].blockPos = getRealCoords(BlockPos(20, 70, 6))
        triviaOptions[1].blockPos = getRealCoords(BlockPos(15, 70, 9))
        triviaOptions[2].blockPos = getRealCoords(BlockPos(10, 70, 6))
    }

    fun onRenderWorld(event: RenderExtractEvent, quizColor: Color, quizDepth: Boolean) {
        if (triviaAnswers == null || triviaOptions.isEmpty()) return
        triviaOptions.forEach { answer ->
            if (!answer.isCorrect) return@forEach
            answer.blockPos?.offset(0, -1, 0)?.let {
                event.drawFilledBox(AABB(it), quizColor, depth = quizDepth)
                event.drawBeaconBeam(it, quizColor)
            }
        }
    }

    fun reset() {
        triviaOptions = MutableList(3) { TriviaAnswer(null, false) }
        triviaAnswers = null
        startTimer(0, 0)
    }
}
package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.logger
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers.quizDepth
import me.odinmain.utils.*
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.Vec3
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object QuizSolver {
    private var answers: MutableMap<String, List<String>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/quizAnswers.json")?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
    private var triviaAnswers: List<String>? = null

    private var triviaOptions: MutableList<TriviaAnswer> = MutableList(3) { TriviaAnswer(null, false) }
    data class TriviaAnswer(var vec3: Vec3?, var correct: Boolean)

    init {
        try {
            val text = isr?.readText()
            answers = gson.fromJson(text, object : TypeToken<MutableMap<String, List<String>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            logger.error("Error loading quiz answers", e)
            answers = mutableMapOf()
        }
    }

    fun onMessage(msg: String) {
        if ((msg.startsWith("[STATUE] Oruo the Omniscient: ") && msg.contains("answered Question #") && msg.endsWith("correctly!")) || msg == "[STATUE] Oruo the Omniscient: I bestow upon you all the power of a hundred years!")
            triviaOptions.forEach { it.correct = false }

        if (msg.trim().startsWithOneOf("ⓐ", "ⓑ", "ⓒ", ignoreCase = true)) {
            if (triviaAnswers?.any { msg.endsWith(it) } ?: return) {
                when (msg.trim()[0]) {
                    'ⓐ' -> triviaOptions[0].correct = true
                    'ⓑ' -> triviaOptions[1].correct = true
                    'ⓒ' -> triviaOptions[2].correct = true
                }
            }
        }

        triviaAnswers = when {
            msg.trim() == "What SkyBlock year is it?" -> listOf("Year ${(((System.currentTimeMillis() / 1000) - 1560276000) / 446400).toInt() + 1}")
            else -> answers.entries.find { msg.contains(it.key) }?.value ?: return
        }
    }

    fun enterRoomQuiz(event: RoomEnterEvent) {
        val room = event.room?.room ?: return
        if (room.data.name != "Quiz") return

        val rotation = room.rotation
        val middleAnswerBlock = room.vec3.addRotationCoords(rotation, 0, 6)

        triviaOptions[0].vec3 = middleAnswerBlock.addRotationCoords(rotation, -5, 3)
        triviaOptions[1].vec3 = middleAnswerBlock
        triviaOptions[2].vec3 = middleAnswerBlock.addRotationCoords(rotation, 5, 3)
    }

    fun renderWorldLastQuiz() {
        if (triviaAnswers == null || triviaOptions.isEmpty() || DungeonUtils.inBoss || !DungeonUtils.inDungeons) return
        triviaOptions.filter { it.correct }.forEach { answer ->
            answer.vec3?.addVec(y= -1)?.let {
                Renderer.drawBox(it.toAABB(), Color.GREEN, depth = quizDepth)
                RenderUtils.drawBeaconBeam(it, Color.GREEN, depth = quizDepth)
            }
        }
    }

    fun reset() {
        triviaOptions = MutableList(3) { TriviaAnswer(null, false) }
        triviaAnswers = null
    }
}
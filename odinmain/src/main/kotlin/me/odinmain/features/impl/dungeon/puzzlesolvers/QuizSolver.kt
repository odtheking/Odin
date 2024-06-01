package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import net.minecraft.util.Vec3
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object QuizSolver {
    private var answers: MutableMap<String, List<String>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/quizAnswers.json")
        ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
    private var triviaAnswers: List<String>? = null

    private var triviaAnswer: MutableList<TriviaAnswer> = MutableList(3) { TriviaAnswer(null, false) }
    data class TriviaAnswer(var vec3: Vec3?, var correct: Boolean)


    init {
        try {
            val text = isr?.readText()
            answers = gson.fromJson(text, object : TypeToken<MutableMap<String, List<String>>>() {}.type)
            isr?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            answers = mutableMapOf()
        }
    }

    fun onMessage(msg: String) {
        if (msg.startsWith("[STATUE] Oruo the Omniscient: ") && msg.contains("answered Question #") && msg.endsWith("correctly!"))
            triviaAnswer = MutableList(3) { TriviaAnswer(null, false) }

        val trimmedMsg = msg.trim()
        if (trimmedMsg.startsWithOneOf("ⓐ", "ⓑ", "ⓒ", ignoreCase = true)) {
            triviaAnswers?.any { msg.endsWith(it) } ?: return
            when {
                trimmedMsg.startsWith("ⓐ", ignoreCase = true) -> triviaAnswer[0].correct = true
                trimmedMsg.startsWith("ⓑ", ignoreCase = true) -> triviaAnswer[1].correct = true
                trimmedMsg.startsWith("ⓒ", ignoreCase = true) -> triviaAnswer[2].correct = true
            }
        }

        triviaAnswers = if (msg.trim() == "What SkyBlock year is it?")
            listOf("Year ${(((System.currentTimeMillis() / 1000) - 1560276000) / 446400).toInt() + 1}")
        else
            answers.entries.find { msg.contains(it.key) }?.value ?: return

    }

    fun enterRoomQuiz(event: EnteredDungeonRoomEvent) {
        val room = event.room?.room ?: return
        val rotation = room.rotation
        if (room.data.name != "Quiz") return

        val middleAnswerBlock = room.vec2.addRotationCoords(rotation, 0, 6).let { Vec3(it.x.toDouble(), 70.0, it.z.toDouble()) }
        val leftAnswerBlock = middleAnswerBlock.addRotationCoords(rotation, 5, 3)
        val rightAnswerBlock = middleAnswerBlock.addRotationCoords(rotation, -5, 3)
        triviaAnswer[1].vec3 = middleAnswerBlock
        triviaAnswer[0].vec3 = leftAnswerBlock
        triviaAnswer[2].vec3 = rightAnswerBlock
    }

    fun renderWorldLastQuiz() {
        triviaAnswer.filter { it.correct }.forEach { answer ->
            answer.vec3?.toAABB()?.let { Renderer.drawBox(it, Color.GREEN, fillAlpha = 0f) }
        }
    }

    fun reset() {
        triviaAnswer = MutableList(3) { TriviaAnswer(null, false) }
        triviaAnswers = null
    }
}
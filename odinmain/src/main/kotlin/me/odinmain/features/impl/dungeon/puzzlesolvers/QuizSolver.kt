package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.startsWithOneOf
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderLivingEvent
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.math.floor

object QuizSolver {
    private var answers: MutableMap<String, List<String>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/quizAnswers.json")
        ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
    private var triviaAnswers: List<String>? = null
    private var triviaAnswer: String? = null

    init {
        try {
            val text = isr?.readText()
            answers = gson.fromJson(
                text, object : TypeToken<MutableMap<String, List<String>>>() {}.type
            )
            isr?.close()
            println(answers.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            answers = mutableMapOf()
        }
    }

    fun onMessage(msg: String) {
        modMessage("Year ${(((System.currentTimeMillis() / 1000) - 1560276000) / 446400).toInt() + 1}")
        if (msg.startsWith("[STATUE] Oruo the Omniscient: ") && msg.contains("answered Question #") && msg.endsWith("correctly!")) triviaAnswer = null
        triviaAnswers = if (msg.trim() == "What SkyBlock year is it?") {
            listOf("Year ${(((System.currentTimeMillis() / 1000) - 1560276000) / 446400).toInt() + 1}")
        } else {
            answers.entries.find { msg.contains(it.key) }?.value ?: return
        }

        if (msg.trim().startsWithOneOf("ⓐ", "ⓑ", "ⓒ")) triviaAnswer = triviaAnswers?.find { msg.endsWith(it) } ?: return
    }

    fun onRenderArmorStandPre(event: RenderLivingEvent.Pre<EntityArmorStand?>) {
        if (triviaAnswer == null || event.entity !is EntityArmorStand) return
        with(event.entity.customNameTag) {
            if (isNotEmpty() && containsOneOf("ⓐ", "ⓑ", "ⓒ")) {
                if (contains(triviaAnswer ?: return))
                    RenderUtils.drawBlockBox(event.entity.position.down(), Color.GREEN, fill = .5f)
                else event.isCanceled = true
            }
        }
    }

    fun reset() {
        triviaAnswer = null
        triviaAnswers = null
    }
}
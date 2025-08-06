package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.ActionSetting
import me.odinmain.clickgui.settings.impl.DropdownSetting
import me.odinmain.clickgui.settings.impl.ListSetting
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.utils.addOrNull
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import me.odinmain.utils.ui.getMCTextHeight
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList
import java.util.regex.PatternSyntaxException

object Countdowns : Module(
    name = "Countdowns",
    description = "Starts a countdown in HUD when you trigger certain chat message, or regex expressions."
) {
    private val hud by HUD("Hud", "Displays something i can't tell.", false) { example ->
        if (example) return@HUD drawStringWidth("Some Countdown: 3.50s", 1f, 1f, Colors.WHITE) to 10f

        var w = 1f
        var h = 1f
        val lineHeight = getMCTextHeight()

        countdowns.forEach {
            w = maxOf(w, drawStringWidth(
                "§r${it.prefix}${it.time.toFixed(divisor = 20)}",
                1f, h, Colors.WHITE
            ))
            h += lineHeight
        }

        if (h == 1f) (0f to 0f) else (w to h)
    }

    data class CountdownTrigger(val prefix: String, val time: Int, val regex: Boolean, val message: String) {
        val a = modMessage("wtf? created")
        @delegate:Transient
        val realRegex: Regex? by lazy {
            if (regex) {
                try {
                    modMessage("Recompiling regex for message: $message")
                    Regex(message)
                } catch (e: PatternSyntaxException) {
                    modMessage("Bad regex for message: $message")
                    null
                }
            } else {
                null
            }
        }
    }
    val countdownTriggers by ListSetting("Countdowns", mutableListOf<CountdownTrigger>())

    private val presetsDropdown by DropdownSetting("Add Presets")
    private val presetQuiz by ActionSetting("Quiz", desc = "wtf") {
        countdownTriggers.addOrNull(CountdownTrigger("§eQuiz: §f", 220, false, "[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know."))
        countdownTriggers.addOrNull(CountdownTrigger("§eQuiz: §f", 140, true, "\\[STATUE\\] Oruo the Omniscient: \\w{1,16} answered Question #[12] correctly!"))
    }.withDependency { presetsDropdown }

    private data class Countdown(val prefix: String, var time: Int)
    private val countdowns = CopyOnWriteArrayList<Countdown>()

    init {
        onMessage(Regex(".*")) { result ->
            countdownTriggers.firstOrNull {
                if (it.regex) (it.realRegex?.let { regex -> result.value.matches(regex) } ?: false) else (it.message == result.value)
            }?.let {
                countdowns.add(Countdown(it.prefix, it.time))
            }
        }

        onWorldLoad {
            countdowns.clear()
        }
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        countdowns.removeIf {
            --it.time <= 0
        }
    }
}

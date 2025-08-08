package me.odinmain.features.impl.skyblock


import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.ActionSetting
import me.odinmain.clickgui.settings.impl.DropdownSetting
import me.odinmain.clickgui.settings.impl.ListSetting
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.utils.addOrNull
import me.odinmain.utils.render.Colors
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import me.odinmain.utils.ui.getMCTextHeight
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

object Countdowns : Module(
    name = "Countdowns",
    description = "Starts a countdown in HUD when you trigger certain chat message, or regex expressions."
) {
    private val hud by HUD("Hud", "Displays something i can't tell.", toggleable = false) { example ->
        if (example) return@HUD drawStringWidth("Some Countdown: 3.50s", 1f, 1f, Colors.WHITE) to 10f
        if (countdowns.isEmpty()) return@HUD 0f to 0f

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

        w to h
    }

    data class CountdownTrigger(val prefix: String, val time: Int, val regex: Boolean, val message: String) {
        @Transient
        var realRegex: Regex? = if (regex) {
            runCatching { Regex(message) }.getOrNull()
        } else {
            null
        }
    }
    val countdownTriggers by ListSetting("Countdowns", mutableListOf<CountdownTrigger>()) { it.copy() }

    private val presetsDropdown by DropdownSetting("Add Presets")
    private val presetQuiz by ActionSetting("Quiz", desc = "Quiz puzzle in dungeons. (2)") {
        countdownTriggers.addOrNull(CountdownTrigger("§eQuiz: §f", 220, false, "[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know."))
        countdownTriggers.addOrNull(CountdownTrigger("§eQuiz: §f", 140, true, "\\[STATUE\\] Oruo the Omniscient: \\w{1,16} answered Question #[12] correctly!"))
    }.withDependency { presetsDropdown }
    private val presetEndIsland by ActionSetting("End Island", desc = "Endstone & Dragon Protector spawn. (2)") {
        countdownTriggers.addOrNull(CountdownTrigger("§eEndstone Protector: §f", 400, false, "The ground begins to shake as an Endstone Protector rises from below!"))
        countdownTriggers.addOrNull(CountdownTrigger("§eDragon: §f", 174, true, "☬ \\w{1,16} placed a Summoning Eye! Brace yourselves! \\(8/8\\)"))
    }.withDependency { presetsDropdown }
    private val presetM3FireFreeze by ActionSetting("M3 Fire Freeze", desc = "The Professor. (1)") {
        countdownTriggers.addOrNull(CountdownTrigger("§eFire Freeze: §f", 106, false, "[BOSS] The Professor: Oh? You found my Guardians' one weakness?"))
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

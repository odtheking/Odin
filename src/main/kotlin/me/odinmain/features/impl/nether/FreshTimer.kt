package me.odinmain.features.impl.nether

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.utils.color
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.buildText

object FreshTimer : Module(
    name = "Fresh Timer",
    description = "Shows the time until fresh timer."
){
    private val notifyFresh by BooleanSetting("Notify Fresh", true, description = "Notifies your party when you get fresh timer.")
    val highlightFresh by BooleanSetting("Highlight Fresh", true, description = "Highlights fresh timer users.")
    val highlightFreshColor by ColorSetting("Highlight Fresh Color", Colors.MINECRAFT_YELLOW, true, description = "Color of the highlight.").withDependency { highlightFresh }

    private val HUD by TextHUD("Fresh Timer") { color, font, shadow ->
        buildText(
            string = "Fresh:",
            supplier = { "${String.format("%.2f", getFreshTimeLeft() / 1000.0)}s" },
            font, color,  color { colorFresh(getFreshTimeLeft()).rgba }, shadow
        )
    }.setting("Displays the time until fresh timer ends.")

    init {
        onMessage(Regex("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!")) {
            val teammate = KuudraUtils.kuudraTeammates.find { it.playerName == mc.thePlayer.name } ?: return@onMessage
            teammate.eatFreshTime = System.currentTimeMillis()
            teammate.eatFresh = true
            if (notifyFresh) modMessage("Fresh tools has been activated")
            if (notifyFresh) partyMessage("FRESH")
            runIn(200) {
                if (notifyFresh) modMessage("Fresh tools has expired")
                teammate.eatFresh = false
            }
        }
    }

    private fun getFreshTimeLeft(): Double {
        val player = KuudraUtils.kuudraTeammates.find { teammate -> teammate.playerName == mc.thePlayer.name } ?: return 0.0
        return ((10000L - (System.currentTimeMillis() - player.eatFreshTime)).takeIf { it > 0 })?.div(1000.0) ?: 0.0
    }

    private fun colorFresh(timeLeft: Double): Color {
        return when {
            timeLeft >= 6 -> Colors.MINECRAFT_GREEN
            timeLeft >= 3 -> Colors.MINECRAFT_GOLD
            else -> Colors.MINECRAFT_RED
        }
    }
}
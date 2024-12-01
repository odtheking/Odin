package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.ui.Colors

object FreshTimer : Module(
    name = "Fresh Timer",
    description = "Shows the time until fresh timer."
){
    private val notifyFresh by BooleanSetting("Notify Fresh", true, description = "Notifies your party when you get fresh timer.")
    val highlightFresh by BooleanSetting("Highlight Fresh", true, description = "Highlights fresh timer users.")
    val highlightFreshColor by ColorSetting("Highlight Fresh Color", Colors.MINECRAFT_YELLOW, true, description = "Color of the highlight.").withDependency { highlightFresh }
    private val freshTimerHUDColor by ColorSetting("Fresh Timer Color", Colors.MINECRAFT_GOLD, true, description = "Color of the fresh timer HUD.")
    /*private val hud by HudSetting("Fresh timer HUD", 10f, 10f, 1f, true) {
        if (it) {
            text("Fresh§f: 9s", 1f, 9f, freshTimerHUDColor, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Fresh: 10s", 12f) + 2f to 16f
        } else {
            val player = KuudraUtils.kuudraTeammates.find { teammate -> teammate.playerName == mc.thePlayer.name } ?: return@HudSetting 0f to 0f
            val timeLeft = 10000L - (System.currentTimeMillis() - player.eatFreshTime)
            if (timeLeft <= 0) return@HudSetting 0f to 0f
            if (player.eatFresh && KuudraUtils.phase == 2)
                text("Fresh§f: ${(timeLeft / 1000.0).round(2)}s", 1f, 9f, freshTimerHUDColor,12f, OdinFont.REGULAR, shadow = true)

            getTextWidth("Fresh: 10s", 12f) + 2f to 12f
        }
    }*/

    init {
        onMessage(Regex("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!")) {
            val teammate = KuudraUtils.kuudraTeammates.find { it.playerName == mc.thePlayer.name } ?: return@onMessage
            teammate.eatFresh = true
            teammate.eatFreshTime = System.currentTimeMillis()
            if (notifyFresh) modMessage("Fresh tools has been activated")
            runIn(200) {
                if (notifyFresh) modMessage("Fresh tools has expired")
                teammate.eatFresh = false
            }
            if (notifyFresh) partyMessage("FRESH")
        }
    }
}
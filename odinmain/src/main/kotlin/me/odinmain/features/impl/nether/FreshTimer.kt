package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.*
import me.odinmain.utils.round
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.*

object FreshTimer : Module(
    name = "Fresh Timer",
    description = "Shows the time until fresh timer.",
    category = Category.NETHER
){
    private val notifyFresh: Boolean by BooleanSetting("Notify Fresh", true, description = "Notifies your party when you get fresh timer.")
    val highlightFresh: Boolean by BooleanSetting("Highlight Fresh", true, description = "Highlights fresh timer users.")
    val highlightFreshColor: Color by ColorSetting("Highlight Fresh Color", Color.YELLOW, true, description = "Color of the highlight.").withDependency { highlightFresh }
    private val freshTimerHUDColor: Color by ColorSetting("Fresh Timer Color", Color.ORANGE, true, description = "Color of the fresh timer HUD.")
    private val hud: HudElement by HudSetting("Fresh timer HUD", 10f, 10f, 1f, true) {
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
    }

    init {
        onMessage("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!", false) {
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
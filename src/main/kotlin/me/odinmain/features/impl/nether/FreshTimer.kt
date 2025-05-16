package me.odinmain.features.impl.nether

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.Colors

object FreshTimer : Module(
    name = "Fresh Timer",
    desc = "Shows the time until fresh timer."
){
    private val notifyFresh by BooleanSetting("Notify Fresh", true, desc = "Notifies your party when you get fresh timer.")
    val highlightFresh by BooleanSetting("Highlight Fresh", true, desc = "Highlights fresh timer users.")
    val highlightFreshColor by ColorSetting("Highlight Fresh Color", Colors.MINECRAFT_YELLOW, true, desc = "Color of the highlight.").withDependency { highlightFresh }
    private val freshTimerHUDColor by ColorSetting("Fresh Timer Color", Colors.MINECRAFT_GOLD, true, desc = "Color of the fresh timer HUD.")
    private val hud by HudSetting("Fresh timer HUD", 10f, 10f, 1f, true) { example ->
        if (example) {
            RenderUtils.drawText("Fresh§f: 9s", 1f, 1f, 1f, freshTimerHUDColor, shadow = true)
            getMCTextWidth("Fresh: 9s") + 2f to 12f
        } else {
            val player = KuudraUtils.kuudraTeammates.find { teammate -> teammate.playerName == mc.thePlayer.name } ?: return@HudSetting 0f to 0f
            val timeLeft = (10000L - (System.currentTimeMillis() - player.eatFreshTime)).takeIf { it > 0 } ?: return@HudSetting 0f to 0f
            if (player.eatFresh && KuudraUtils.phase == 2)
                RenderUtils.drawText("Fresh§f: ${(timeLeft / 1000f).toFixed()}s", 1f, 1f, 1f, highlightFreshColor, shadow = true)

            getMCTextWidth("Fresh: 10s") + 2f to 12f
        }
    }

    init {
        onMessage(Regex("^Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!$")) {
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
}
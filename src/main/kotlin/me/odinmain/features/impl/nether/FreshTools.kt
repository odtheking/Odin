package me.odinmain.features.impl.nether

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.getTextWidth

object FreshTools : Module(
    name = "Fresh Tools",
    description = "Shows the time until fresh timer."
){
    private val notifyFresh by BooleanSetting("Notify Fresh", true, desc = "Notifies your party when you get fresh timer.")
    val highlightFresh by BooleanSetting("Highlight Fresh", true, desc = "Highlights fresh timer users.")
    val highlightFreshColor by ColorSetting("Highlight Fresh Color", Colors.MINECRAFT_YELLOW, true, desc = "Color of the highlight.").withDependency { highlightFresh }
    private val hud by HUD("Fresh timer", "Displays how long players have fresh for.") { example ->
        if (!example && (!KuudraUtils.inKuudra || KuudraUtils.phase != 2 || KuudraUtils.freshers.isEmpty())) return@HUD 0f to 0f

        var yOffset = 1f
        var maxWidth = 0f

        if (example) {
            RenderUtils.drawText("§6Player1§f: 9s", 1f, yOffset)
            yOffset += 10f
            RenderUtils.drawText("§6Player2§f: 5s", 1f, yOffset)
            maxWidth = getTextWidth("Player2: 5s") + 2f
            yOffset += 10f
        } else {
            KuudraUtils.freshers.forEach { fresher ->
                val timeLeft = fresher.value?.let { (10000L - (System.currentTimeMillis() - it)) }?.takeIf { it > 0 } ?: return@forEach
                val text = "§6${fresher.key}§f: ${(timeLeft / 1000f).toFixed()}s"
                RenderUtils.drawText(text, 1f, yOffset)
                maxWidth = maxOf(maxWidth, getTextWidth(text) + 2f)
                yOffset += 10f
            }
        }

        maxWidth to yOffset
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
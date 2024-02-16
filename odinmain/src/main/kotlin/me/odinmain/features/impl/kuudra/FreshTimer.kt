package me.odinmain.features.impl.kuudra

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.ui.util.getTextWidth
import me.odinmain.ui.util.text
import me.odinmain.utils.render.Color
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object FreshTimer : Module(
    name = "Fresh Timer",
    description = "Shows the time until fresh tools",
    category = Category.KUUDRA
){
    private val notifyFresh: Boolean by BooleanSetting("Notify Fresh", true, description = "Notifies your party when you get fresh tools")
    val highlightFresh: Boolean by BooleanSetting("Highlight Fresh", true, description = "Highlights fresh tools users")
    val highlightFreshColor: Color by ColorSetting("Highlight Fresh Color", Color.YELLOW, true).withDependency { highlightFresh }
    val notifyOtherFresh: Boolean by BooleanSetting("Notify Other Fresh", true, description = "Notifies you when someone else gets fresh tools")
    private val hud: HudElement by HudSetting("Fresh tools timer", 10f, 10f, 1f, true) {
        if (it) {
            text("§4Fresh Tools§f: 1000ms", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR)
            getTextWidth("Fresh Tools: 1000ms", 12f) + 2f to 16f
        } else {
            val player = KuudraUtils.kuudraTeammates.find { it.playerName == mc.thePlayer.name } ?: return@HudSetting 0f to 0f

            if (player.eatFresh)
                text("§4Fresh Tools§f: ${10000L - (System.currentTimeMillis() - player.eatFreshTime)}ms", 1f, 9f, Color.WHITE,12f, OdinFont.REGULAR)

            getTextWidth("Fresh Tools: 1000ms", 12f) + 2f to 12f
        }
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        when (event.message) {
            "Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!" -> {
                val teammate = KuudraUtils.kuudraTeammates.find { it.playerName == mc.thePlayer.name } ?: return
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
}
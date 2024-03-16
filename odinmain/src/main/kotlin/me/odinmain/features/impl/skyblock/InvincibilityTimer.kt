package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.skyblock.partyMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    description = "Timer to show how long you have left Invincible.",
    category = Category.SKYBLOCK
)  {
    private val invincibilityAnnounce: Boolean by BooleanSetting("Announce Invincibility", default = true, description = "Announces when you get invincibility")
    private val hud: HudElement by HudSetting("Invincibility timer HUD", 10f, 10f, 1f, true) {
        if (it) {
            text("§bBonzo§f: 59t", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Bonzo: 59t", 12f) + 2f to 16f
        } else {
            if (invincibilityTime.first <= 0) return@HudSetting 0f to 0f
            if (invincibilityTime.second == "Bonzo") {
                text("§bBonzo§f: ${invincibilityTime.first }t", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            } else if (invincibilityTime.second == "Phoenix") {
                text("§6Phoenix§f: ${invincibilityTime.first}t", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            }

            getTextWidth("Bonzo: 59t", 12f) + 2f to 12f
        }
    }
    private var invincibilityTime = Pair(0, "")
    private val bonzoMaskRegex = Regex("^Your (?:. )?Bonzo's Mask saved your life!$")
    private val phoenixPetRegex = Regex("^Your Phoenix Pet saved you from certain death!$")
    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val msg = event.message
        if (!msg.matches(bonzoMaskRegex) && !msg.matches(phoenixPetRegex)) return

        val invincibilityType =
            if (msg.contains("Bonzo's Mask")) "Bonzo"
            else "Phoenix"
        if (invincibilityAnnounce) partyMessage("pc $invincibilityType Procced (3s) ")
        invincibilityTime = Pair(60, invincibilityType)
    }
    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (invincibilityTime.first > 0) {
            invincibilityTime = Pair(invincibilityTime.first - 1, invincibilityTime.second)
        }
    }
}
package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.RealServerTick
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
    private val hud: HudElement by HudSetting("Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            text("§bBonzo§f: 59t", 1f, 7f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Bonzo: 59t", 12f) + 2f to 16f
        } else {
            if (invincibilityTime.time <= 0) return@HudSetting 0f to 0f
            if (invincibilityTime.type == "Bonzo") {
                text("§bBonzo§f: ${invincibilityTime.time }t", 1f, 7f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            } else if (invincibilityTime.type == "Phoenix") {
                text("§6Phoenix§f: ${invincibilityTime.time}t", 1f, 7f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            }

            getTextWidth("Bonzo: 59t", 12f) + 2f to 12f
        }
    }
    data class Timer(var time: Int, var type: String)
    private var invincibilityTime = Timer(0, "")
    private val bonzoMaskRegex = Regex("^Your (?:. )?Bonzo's Mask saved your life!$")
    private val phoenixPetRegex = Regex("^Your Phoenix Pet saved you from certain death!$")
    private val spiritPetRegex = Regex("^Second Wind Activated! Your Spirit Mask saved your life!\$")

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val msg = event.message
        if (!msg.matches(bonzoMaskRegex) && !msg.matches(phoenixPetRegex) && !msg.matches(spiritPetRegex)) return

        val invincibilityType = if (msg.contains("Bonzo's Mask")) "Bonzo" else if (msg.contains("Phoenix")) "Phoenix" else "Spirit"
        if (invincibilityAnnounce) partyMessage("pc $invincibilityType Procced")
        invincibilityTime = Timer(60, invincibilityType)
    }
    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        invincibilityTime.time--
    }
}
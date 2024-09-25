package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.matchesOneOf
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.partyMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    description = "Timer to show how long you have left Invincible.",
    category = Category.SKYBLOCK
)  {
    private val invincibilityAnnounce: Boolean by BooleanSetting("Announce Invincibility", default = true, description = "Announces when you get invincibility.")
    private val showPrefix: Boolean by BooleanSetting("Show Prefix", default = true, description = "Shows the prefix of the timer.")
    private val hud: HudElement by HudSetting("Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            mcText("${if(showPrefix) "§bBonzo§f: " else ""}59t", 1f, 1f, 1, Color.WHITE, center = false)
            getMCTextWidth("Bonzo: 59t") + 2f to 10f
        } else {
            if (invincibilityTime.time <= 0) return@HudSetting 0f to 0f
            val invincibilityType = if (invincibilityTime.type == "Bonzo") "§bBonzo§f:" else if (invincibilityTime.type == "Phoenix") "§6Phoenix§f:" else "§5Spirit§f:"

            mcText("${if (showPrefix) invincibilityType else ""} ${invincibilityTime.time}t", 1f, 1f, 1, Color.WHITE, center = false)
            getMCTextWidth("Bonzo: 59t") + 2f to 1f
        }
    }

    private data class Timer(var time: Int, var type: String)
    private var invincibilityTime = Timer(0, "")
    private val bonzoMaskRegex = Regex("^Your (?:. )?Bonzo's Mask saved your life!$")
    private val phoenixPetRegex = Regex("^Your Phoenix Pet saved you from certain death!$")
    private val spiritPetRegex = Regex("^Second Wind Activated! Your Spirit Mask saved your life!\$")

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (!event.message.matchesOneOf(bonzoMaskRegex, phoenixPetRegex, spiritPetRegex)) return

        val invincibilityType = if (event.message.contains("Bonzo's Mask")) "Bonzo" else if (event.message.contains("Phoenix")) "Phoenix" else "Spirit"
        if (invincibilityAnnounce) partyMessage("$invincibilityType Procced")
        invincibilityTime = Timer(60, invincibilityType)
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        invincibilityTime.time--
    }
}
package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.GuiEvent.DrawSlotOverlayEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.skyblock.skyblockID
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object InvincibilityTimer : Module(
    name = "Invincibility Timer",
    description = "Timer to show how long you have left Invincible.",
    category = Category.SKYBLOCK
)  {
    private val showCooldown by BooleanSetting("Show Cooldown", default = true, description = "Shows the cooldown of the mask.")
    private val invincibilityAnnounce by BooleanSetting("Announce Invincibility", default = true, description = "Announces when you get invincibility.")
    private val hud by HudSetting("Timer Hud", 10f, 10f, 1f, true) {
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
    private val showPrefix by BooleanSetting("Show Prefix", default = true, description = "Shows the prefix of the timer.")

    private data class Timer(var time: Int, var type: String)
    private var invincibilityTime = Timer(0, "")
    private val bonzoMaskRegex = Regex("^Your (?:. )?Bonzo's Mask saved your life!$")
    private val phoenixPetRegex = Regex("^Your Phoenix Pet saved you from certain death!$")
    private val spiritPetRegex = Regex("^Second Wind Activated! Your Spirit Mask saved your life!\$")

    private var spiritMaskProc = 0L
    private var bonzoMaskProc = 0L

    init {
        onWorldLoad {
            invincibilityTime = Timer(0, "")
            spiritMaskProc = 0L
            bonzoMaskProc = 0L
        }
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val type = when {
            event.message.matches(bonzoMaskRegex) -> {
                bonzoMaskProc = System.currentTimeMillis()
                "Bonzo"
            }
            event.message.matches(spiritPetRegex) -> {
                spiritMaskProc = System.currentTimeMillis()
                "Spirit"
            }
            event.message.matches(phoenixPetRegex) -> "Phoenix"
            else -> return
        }

        if (invincibilityAnnounce) partyMessage("$type Procced")
        invincibilityTime = Timer(60, type)
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        invincibilityTime.time--
    }

    @SubscribeEvent
    fun onRenderSlotOverlay(event: DrawSlotOverlayEvent) {
        if (!LocationUtils.isInSkyblock || !showCooldown) return
        val durability = when (event.stack.skyblockID) {
            "BONZO_MASK", "STARRED_BONZO_MASK" -> (System.currentTimeMillis() - bonzoMaskProc) / 180_000.0
            "SPIRIT_MASK", "STARRED_SPIRIT_MASK" -> (System.currentTimeMillis() - spiritMaskProc) / 30_000.0
            else -> return
        }.takeIf { it < 1.0 } ?: return
        RenderUtils.renderDurabilityBar(event.x ?: return, event.y ?: return, durability)
    }
}
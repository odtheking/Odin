package me.odinmain.features.impl.nether

import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.itemID
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EnrageDisplay : Module(
    name = "Enrage Display",
    description = "Timer for cooldown of reaper armor enrage.",
    category = Category.NETHER
) {
    private val hud: HudElement by HudSetting("Enrage Timer Hud", 10f, 10f, 1f, false) {
        if (it) {
            text("§4Enrage: §a119t", 1f, 9f, Color.RED, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Enrage: 119t", 12f) + 2f to 16f
        } else {
            val colorCode = when {
                enrageTimer >= 60 -> "§a"
                enrageTimer in 30..60 -> "§e"
                enrageTimer in 0..30 -> "§c"
                else -> return@HudSetting 0f to 0f
            }
            text("§4Enrage: ${colorCode}${enrageTimer}t", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Enrage: 119t", 12f) + 2f to 12f
        }
    }

    private var enrageTimer = -1

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName == "mob.zombie.remedy" && it.pitch == 1.0f && it.volume == 0.5f && mc.thePlayer?.getCurrentArmor(0)?.itemID == "REAPER_BOOTS" &&
                mc.thePlayer?.getCurrentArmor(1)?.itemID == "REAPER_LEGGINGS" && mc.thePlayer?.getCurrentArmor(2)?.itemID == "REAPER_CHESTPLATE")
                    enrageTimer = 120
        }
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        enrageTimer--
    }
}
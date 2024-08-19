package me.odinmain.features.impl.nether

import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.mcTextAndWidth
import me.odinmain.utils.skyblock.itemID
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TacTimer : Module(
    name = "Tac Timer",
    category = Category.NETHER,
    description = "Tactical Insertion Timer"
) {
    private val hud: HudElement by HudSetting("Timer Hud", 10f, 10f, 1f, false) {
        if (it) {
            val width = mcTextAndWidth("§6Tac: 59t", 1f, 1f, 1f, color = Color.WHITE, center = false)
            width + 2f to 12f
        } else {
            val colorCode = when {
                tacTimer >= 40 -> "§a"
                tacTimer in 20..40 -> "§e"
                tacTimer in 0..20 -> "§c"
                else -> return@HudSetting 0f to 0f
            }
            val width = mcTextAndWidth("§6Tac: ${colorCode}${tacTimer}t", 1f, 1f, 1f, color = Color.WHITE, center = false)
            width + 2f to 12f
        }
    }

    private var tacTimer = -1

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (mc.thePlayer?.heldItem?.itemID != "TACTICAL_INSERTION" || it.soundName != "fire.ignite" || it.volume != 1f || it.pitch != 0.74603176f) return@onPacket
            tacTimer = 60
        }
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        tacTimer--
    }
}
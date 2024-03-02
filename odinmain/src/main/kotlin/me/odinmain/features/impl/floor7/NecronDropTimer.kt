package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.render.Color
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NecronDropTimer : Module(
    name = "Necron Drop Timer",
    description = "Shows a timer for when Necron drops you down.",
    category = Category.FLOOR7
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            text("§4Necron dropping in §a65", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Necron dropping in 65", 12f) + 2f to 16f
        } else if (timer > 0) {
            text("§4Necron dropping in ${colorizeTime(timer)}$timer", 1f, 9f, Color.WHITE,12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Necron dropping in $timer", 12f) + 2f to 12f
        } else 0f to 0f
    }

    private fun colorizeTime(time: Byte): String {
        return when {
            time > 30 -> "§a"
            time > 10 -> "§e"
            else -> "§c"
        }
    }

    private var timer: Byte = 0

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (timer > 0) {
            timer--
        }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        if (event.message == "[BOSS] Necron: I'm afraid, your journey ends now.") timer = 60
    }
}
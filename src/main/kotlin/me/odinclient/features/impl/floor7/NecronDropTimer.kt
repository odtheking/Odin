package me.odinclient.features.impl.floor7

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.events.impl.ServerTickEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.render.gui.nvg.getTextWidth
import me.odinclient.utils.render.gui.nvg.textWithControlCodes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NecronDropTimer : Module(
    name = "Necron Drop Timer",
    description = "Shows a timer for when Necron drops you down.",
    category = Category.FLOOR7
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            textWithControlCodes(
                "§4Necron dropping in §a65",
                1f,
                9f,
                16f,
                Fonts.REGULAR
            )
            getTextWidth("Necron dropping in 65", 16f, Fonts.REGULAR) + 2f to 16f
        } else if (timer > 0) {
            textWithControlCodes(
                "§4Necron dropping in ${colorizeTime(timer)}$timer",
                1f,
                9f,
                16f,
                Fonts.REGULAR
            )
            getTextWidth("Necron dropping in $timer", 16f, Fonts.REGULAR) + 2f to 16f
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
package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.FontRenderer.drawString
import me.odinmain.font.FontRenderer.getWidth
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.render.Color
import me.odinmain.utils.round
import kotlin.math.max

object Server : Module(
    name = "Server Hud",
    category = Category.RENDER,
    description = "Displays your current ping and the server's TPS."
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            drawString("§ePing §a60ms", 0f, 0f, Color.WHITE, true)
            drawString("§3TPS §a20.0", 0f, 9f, Color.WHITE, true)
            max(
                getWidth("Ping 60ms"),
                getWidth("TPS 20.0")
            ) + 2f to 18f
        } else {
            drawString("§ePing ${colorizePing(ServerUtils.averagePing.toInt())}ms", 0f, 0f, Color.WHITE, true)
            drawString("§3TPS ${colorizeTps(ServerUtils.averageTps.round(1))}", 0f, 9f, Color.WHITE, true)
            max(
                getWidth("Ping 60ms"),
                getWidth("TPS 20.0")
            ) + 2f to 33f
        }
    }

    private fun colorizePing(ping: Int): String {
        return when {
            ping < 150 -> "§a$ping"
            ping < 200 -> "§e$ping"
            ping < 250 -> "§c$ping"
            else -> "§4$ping"
        }
    }

    private fun colorizeTps(tps: Double): String {
        return when {
            tps > 18.0 -> "§a$tps"
            tps > 15.0 -> "§e$tps"
            tps > 10.0 -> "§c$tps"
            else -> "§4$tps"
        }
    }
}
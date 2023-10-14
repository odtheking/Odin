package me.odinmain.features.impl.render

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.render.gui.nvg.getTextWidth
import me.odinmain.utils.render.gui.nvg.textWithControlCodes
import me.odinmain.utils.round
import kotlin.math.max

object Server : Module(
    name = "Server Hud",
    category = Category.RENDER,
    description = "Displays your current ping and the server's TPS."
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            textWithControlCodes("§6Ping §a60ms", 1f, 9f, 16f, Fonts.REGULAR)
            textWithControlCodes("§3TPS §a20.0", 1f, 26f, 16f, Fonts.REGULAR)
            max(getTextWidth("Ping 60ms", 16f, Fonts.REGULAR),
                getTextWidth("TPS 20.0", 16f, Fonts.REGULAR)
            ) + 2f to 33f
        } else {
            textWithControlCodes("§6Ping §a${colorizePing(ServerUtils.averagePing.toInt())}ms", 1f, 9f, 16f, Fonts.REGULAR)
            textWithControlCodes("§3TPS §a${colorizeTps(ServerUtils.averageTps.round(1))}", 1f, 26f, 16f, Fonts.REGULAR)

            max(getTextWidth("Ping ${colorizePing(ServerUtils.averagePing.toInt())}ms", 16f, Fonts.REGULAR),
                getTextWidth("TPS ${colorizeTps(ServerUtils.averageTps)}", 16f, Fonts.REGULAR)
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
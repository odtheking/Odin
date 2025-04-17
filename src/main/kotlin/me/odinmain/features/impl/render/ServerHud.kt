package me.odinmain.features.impl.render

import me.odinmain.OdinMain
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.render.*
import me.odinmain.utils.round
import me.odinmain.utils.ui.Colors
import org.apache.commons.lang3.math.IEEE754rUtils.max

object ServerHud : Module(
    name = "Server Hud",
    desc = "Displays your current ping, FPS and server's TPS."
) {
    private val ping by BooleanSetting("Ping", true, desc = "Displays your current ping.")
    private val tps by BooleanSetting("TPS", true, desc = "Displays the server's TPS.")
    private val fps by BooleanSetting("FPS", false, desc = "Displays your current FPS.")
    private val style by SelectorSetting("Style", "Row", arrayListOf("Row", "Stacked"), desc = "The style of the server hud.")

    val hud by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            if (style == 0) {
                var width = 0f
                if (tps) {
                    width += getMCTextWidth("§rTPS: §f20") * 1.5f
                    RenderUtils.drawText("§rTPS: §f20", 1f, 5f, 2f, ClickGUIModule.color, shadow = true, center = false)
                }
                if (fps) {
                    width += getMCTextWidth("§rFPS: §f240") * 1.5f
                    RenderUtils.drawText("§rFPS: §f240", 5f + if (tps) getMCTextWidth("§rTPS: §f20") * 2f else 0f, 5f, 2f, ClickGUIModule.color, shadow = true, center = false)
                }
                if (ping) {
                    width += getMCTextWidth("§rPing: §f60") * 1.5f
                    RenderUtils.drawText("§rPing: §f60", 5f + if (tps) getMCTextWidth("§rTPS: §f20") * 4.5f else 0f + if (fps) getMCTextWidth("§rFPS: §f240") * 3f + 5f else 0f, 5f, 2f, ClickGUIModule.color, shadow = true, center = false)
                }
                width + 6f to if (ping || tps || fps) getMCTextHeight() * 2 + 6f else 0f
            } else {
                if (ping) RenderUtils.drawText("§6Ping: §a60ms", 1f, 9f, 2f, Colors.WHITE, shadow = true, center = false)
                if (tps) RenderUtils.drawText("§3TPS: §a20.0", 1f, 26f, 2f, Colors.WHITE, shadow = true, center = false)
                if (fps) RenderUtils.drawText("§dFPS: §a240", 1f, 43f, 2f, Colors.WHITE, shadow = true, center = false)
                max(
                    if (ping) getTextWidth("Ping: 60ms", 12f) else 0f,
                    if (tps) getTextWidth("TPS: 20.0", 12f) else 0f,
                    if (fps) getTextWidth("§dFPS: §a240.0", 12f) else 0f
                ) + 2f to if (ping && tps && fps) 50f else if (ping && tps || ping && fps || tps && fps) 35f else 20f
            }
        } else {
            if (style == 0) {
                val fpsText = "§rFPS: §f${OdinMain.mc.debug.split(" ")[0].toIntOrNull() ?: 0}"
                val pingText = "§rPing: §f${ServerUtils.averagePing.toInt()}"
                val tpsText = "§rTPS: §f${if (ServerUtils.averageTps > 19.3) 20 else ServerUtils.averageTps.toInt()}"
                var width = 0f
                if (tps)
                    width += mcTextAndWidth(tpsText, 1f, 5f, 2, ClickGUIModule.color, shadow = true, center = false) * 1.5f
                if (fps)
                    width += mcTextAndWidth(fpsText, 5f + (if (tps) getMCTextWidth(tpsText) * 2f else 0f), 5f, 2, ClickGUIModule.color, shadow = true, center = false) * 1.5f
                if (ping)
                    width += mcTextAndWidth(pingText, 5f + (if (tps) getMCTextWidth(tpsText) * 2f else 0f) + (if (fps) getMCTextWidth(fpsText) * 2f + 5f else 0f), 5f, 2, ClickGUIModule.color, shadow = true, center = false) * 1.5f
                width + 2f to if (ping || tps || fps) getMCTextWidth("A") + 6f else 0f
            } else {
                if (ping) RenderUtils.drawText("§6Ping: §a${ServerUtils.averagePing.toInt()}ms", 1f, 9f, 2f, Colors.WHITE, shadow = true, center = false)
                if (tps) RenderUtils.drawText("§3TPS: §a${ServerUtils.averageTps.round(1)}", 1f, 26f, 2f, Colors.WHITE, shadow = true, center = false)
                if (fps) RenderUtils.drawText("§dFPS: §a${mc.debug.split(" ")[0].toIntOrNull() ?: 0}", 1f, 43f, 2f, Colors.WHITE, shadow = true, center = false)
                max(
                    if (ping) getTextWidth("§ePing: ${colorizePing(ServerUtils.averagePing.toInt())}ms", 12f) else 0f,
                    if (tps) getTextWidth("§ePing: ${colorizePing(ServerUtils.averagePing.toInt())}ms", 12f) else 0f,
                    if (fps) getTextWidth("§dFPS: ${colorizeFPS(mc.debug.split(" ")[0].toIntOrNull() ?: 0)}", 12f) else 0f
                ) + 2f to if (ping && tps && fps) 50f else if (ping && tps || ping && fps || tps && fps) 35f else 20f
            }
        }
    }

    fun colorizePing(ping: Int): String {
        return when {
            ping < 150 -> "§a$ping"
            ping < 200 -> "§e$ping"
            ping < 250 -> "§c$ping"
            else -> "§4$ping"
        }
    }

    fun colorizeTps(tps: Double): String {
        return when {
            tps > 18.0 -> "§a$tps"
            tps > 15.0 -> "§e$tps"
            tps > 10.0 -> "§c$tps"
            else -> "§4$tps"
        }
    }

    fun colorizeFPS(fps: Int): String {
        return when {
            fps > 200 -> "§a$fps"
            fps > 100.0 -> "§e$fps"
            fps > 60.0 -> "§c$fps"
            else -> "§4$fps"
        }
    }
}
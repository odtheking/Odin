package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.max
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getTextHeight
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.round

object ServerDisplay : Module(
    name = "Server Hud",
    category = Category.RENDER,
    description = "Displays your current ping and the server's TPS."
) {
    private val ping: Boolean by BooleanSetting("Ping", true)
    private val tps: Boolean by BooleanSetting("TPS", true)
    private val fps: Boolean by BooleanSetting("FPS", false)
    private val style: Int by SelectorSetting("Style", "Odin", arrayListOf("Odin", "Gringo Client"))

    val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            if (style == 1) {
                var width = 0f
                if (tps) {
                    width += getTextWidth("§rTPS: §f20.0", 14f) * 1.5f
                    text("§rTPS: §f20.0", 1f, 10f, ClickGUIModule.color, 14f)
                }
                if (fps) {
                    width += getTextWidth("§rFPS: §f240", 14f) * 1.5f
                    text("§rFPS: §f240", 1f + if (tps) getTextWidth("§rTPS: §f20.0", 14f) * 1.5f else 0f, 10f, ClickGUIModule.color, 14f)
                }
                if (ping) {
                    width += getTextWidth("§rPing: §f60", 12f) * 1.5f
                    text("§rPing: §f60", 1f + (if (tps) getTextWidth("§rTPS: §f20.0", 14f) * 1.5f else 0f) + (if (fps) getTextWidth("§rFPS: §f240", 14f) * 1.5f else 0f), 10f, ClickGUIModule.color, 14f)
                }
                width + 2f to if (ping || tps || fps) getTextHeight("A", 14f) + 6f else 0f
            } else {
                if (ping) text("§6Ping: §a60ms", 1f, 9f, Color.WHITE,12f)
                if (tps) text("§3TPS: §a20.0", 1f, 26f, Color.WHITE,12f)
                if (fps) text("§dFPS: §a240.0", 1f, 43f, Color.WHITE,12f)
                max(
                    if (ping) getTextWidth("Ping: 60ms", 12f) else 0f,
                    if (tps) getTextWidth("TPS: 20.0", 12f) else 0f,
                    if (fps) getTextWidth("§dFPS: §a240.0", 12f) else 0f
                ) + 2f to if (ping && tps && fps) 50f else if (ping && tps || ping && fps || tps && fps) 35f else 20f
            }
        } else {
            if (style == 1) {
                val fpsText = "§rFPS: §f${mc.debug.split(" ")[0].toIntOrNull() ?: 0}"
                val pingText = "§rPing: §f${ServerUtils.averagePing.toInt()}"
                val tpsText = "§rTPS: §f${ServerUtils.averageTps.round(1)}"
                var width = 0f
                if (tps)
                    text(tpsText, 1f, 10f, ClickGUIModule.color, 14f)
                    width += getTextWidth(tpsText, 14f) * 1.5f
                if (fps)
                    text(fpsText, 1f + (if (tps) getTextWidth(tpsText, 14f) * 1.5f else 0f), 10f, ClickGUIModule.color, 14f)
                    width += getTextWidth(fpsText, 14f) * 1.5f
                if (ping)
                    text(pingText, 1f + (if (tps) getTextWidth(tpsText, 14f) * 1.5f else 0f) + (if (fps) getTextWidth(fpsText, 14f) * 1.5f else 0f), 10f, ClickGUIModule.color, 14f)
                    width += getTextWidth(pingText, 14f) * 1.5f
                width + 2f to if (ping || tps || fps) getTextHeight("A", 14f) + 6f else 0f
            } else {
                if (ping) text("§6Ping: ${colorizePing(ServerUtils.averagePing.toInt())}ms", 1f, 9f, Color.WHITE,12f)
                if (tps) text("§3TPS: ${colorizeTps(ServerUtils.averageTps.round(1))}", 1f, 26f, Color.WHITE,12f)
                if (fps) text("§dFPS: ${colorizeFPS(mc.debug.split(" ")[0].toIntOrNull() ?: 0)}", 1f, 43f, Color.WHITE,12f)
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
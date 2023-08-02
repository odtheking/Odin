package me.odinclient.features.impl.general

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.ui.hud.HudData
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.TextHud
import me.odinclient.utils.ServerUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import kotlin.math.floor

object Server : Module(
    name = "Server",
    category = Category.GENERAL,
    description = "Server related features."
) {
    private val hud: HudData by HudSetting("Server Hud", ServerHud)

    object ServerHud : TextHud() {
        override fun getLines(example: Boolean): MutableList<String> {
            return if (example) {
                mutableListOf(
                    "§6Ping §a60ms",
                    "§3TPS §a20.0"
                )
            } else {
                mutableListOf(
                    "§6Ping ${colorizePing(floor(ServerUtils.averagePing * 10) / 10)}ms",
                    "§3TPS ${colorizeTps(floor(ServerUtils.averageTps * 10) / 10)}"
                )
            }
        }

        private fun colorizePing(ping: Double): String {
            return when {
                ping < 150.0 -> "§a$ping"
                ping < 200.0 -> "§e$ping"
                ping < 250.0 -> "§c$ping"
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
}
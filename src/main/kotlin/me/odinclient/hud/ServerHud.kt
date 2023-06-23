package me.odinclient.hud

import cc.polyfrost.oneconfig.hud.TextHud
import me.odinclient.utils.Server
import kotlin.math.floor

class ServerHud: TextHud(false) {

    override fun getLines(lines: MutableList<String>?, example: Boolean) {
        if (example) {
            lines?.add(0, "§6Ping §a60ms")
            lines?.add(1, "§3TPS §a20.0")
        } else {
            lines?.add(0, "§6Ping ${colorizePing(floor(Server.averagePing * 10) / 10)}ms")
            lines?.add(1, "§3TPS ${colorizeTps(floor(Server.averageTps * 10) / 10)}")
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
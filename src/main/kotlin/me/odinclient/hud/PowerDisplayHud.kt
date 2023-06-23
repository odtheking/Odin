package me.odinclient.hud

import cc.polyfrost.oneconfig.hud.TextHud
import me.odinclient.features.dungeon.BlessingDisplay

class PowerDisplayHud: TextHud(false) {
    override fun getLines(lines: MutableList<String>?, example: Boolean) {
        if (example) {
            lines?.add(0, "§cPower §a29")
            lines?.add(1, "§cT§6i§am§5e §a5")
            return
        }
        BlessingDisplay.Blessings.values().forEach {
            if (it.current != 0)
                lines?.add(0, "${it.displayString} §a${it.current}")
        }
    }
}
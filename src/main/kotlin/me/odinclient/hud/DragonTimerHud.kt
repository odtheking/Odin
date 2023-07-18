package me.odinclient.hud

import cc.polyfrost.oneconfig.hud.TextHud
import me.odinclient.features.impl.m7.DragonTimer.toRender

class DragonTimerHud: TextHud(false) {
    override fun getLines(lines: MutableList<String>?, example: Boolean) {
        if (example) {
            lines?.add(0, "§6Purple spawning in §a4500ms")
            lines?.add(1, "§cRed spawning in §e1200ms")
        } else if (toRender.size != 0) {
            toRender.forEachIndexed { index, triple ->
                lines?.add(index, triple.first)
            }
        }
    }
}
package me.odinclient.hud

import cc.polyfrost.oneconfig.hud.TextHud
import me.odinclient.features.dungeon.CanClip

class CanClipHud: TextHud(false) {
    override fun getLines(lines: MutableList<String>?, example: Boolean) {
        if (example) lines?.add(0, "Can Clip")
        else if (CanClip.canClip) lines?.add(0, "Can Clip")
    }
}
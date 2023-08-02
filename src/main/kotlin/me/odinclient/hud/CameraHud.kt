package me.odinclient.hud

import me.odinclient.ui.hud.TextHud

object CameraHud : TextHud(200f, 200f) {
    override fun getLines(example: Boolean): MutableList<String> {
        return mutableListOf(if (example) {
            "Example Camera Hud"
        } else {
            "CameraHud"
        })
    }
}
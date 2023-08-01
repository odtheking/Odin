package me.odinclient.hud

import me.odinclient.ui.hud.TextHud

class CameraHud : TextHud(0f, 0f) {
    override fun getLines(example: Boolean): MutableList<String> {
        return if (example) {
            mutableListOf(
                "Example Camera Hud"
            )
        } else mutableListOf(
            "CameraHud"
        )
    }
}
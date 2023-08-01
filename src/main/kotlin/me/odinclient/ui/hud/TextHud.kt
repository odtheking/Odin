package me.odinclient.ui.hud

import me.odinclient.OdinClient.Companion.mc

open class TextHud(x: Float, y: Float) : BaseHud(x, y) {
    private var lines: MutableList<String> = mutableListOf()

    override fun draw(example: Boolean) {
        lines = getLines(example)

        var yOffset = 0f
        lines.forEach { line ->
            mc.fontRendererObj.drawStringWithShadow(line, 2f, y + yOffset, 0xffffff)
            yOffset += 12f
        }
    }

    open fun getLines(example: Boolean): MutableList<String> {
        return mutableListOf()
    }
}
package me.odinclient.ui.hud

import me.odinclient.OdinClient.Companion.mc

@Deprecated("dont use this.")
open class TextHud(x: Float = 0f, y: Float = 0f) : BaseHud(x, y) {
    var lines: MutableList<String> = mutableListOf()

    override fun draw(example: Boolean): Pair<Float, Float> {
        lines = getLines(example)

        var yOffset = 0f
        var width = 0f
        lines.forEach { line ->
            width = width.coerceAtLeast(mc.fontRendererObj.getStringWidth(line).toFloat())
            mc.fontRendererObj.drawStringWithShadow(line, 0f, yOffset, 0xffffff)
            yOffset += 12f
        }
        return Pair(width, yOffset)
    }

    open fun getLines(example: Boolean): MutableList<String> {
        return mutableListOf()
    }

}
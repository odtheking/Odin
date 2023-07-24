package me.odinclient.utils.render.gui.animations.impl

import me.odinclient.utils.render.gui.animations.Animation
import java.awt.Color

class ColorAnimation(duration: Long): Animation<Color>(duration) {

    override fun get(start: Color, end: Color, reverse: Boolean): Color {
        if (!isAnimating()) return if (reverse) start else end
        return Color(
            calculate(start.red, end.red, reverse),
            calculate(start.green, end.green, reverse),
            calculate(start.blue, end.blue, reverse),
        )
    }

    private fun calculate(start: Int, end: Int, reverse: Boolean) =
        ((if (reverse) end + (start - end) else start + (end - start)) * getPercent() / 100f).toInt().coerceIn(0, 255)
}
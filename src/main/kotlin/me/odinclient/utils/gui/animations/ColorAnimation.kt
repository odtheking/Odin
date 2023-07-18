package me.odinclient.utils.gui.animations

import java.awt.Color

class ColorAnimation(duration: Long): Animation<Color>(duration) {

    override fun get(start: Color, end: Color, reverse: Boolean): Color {
        val (startC, endC) = if (reverse) listOf(end, start) else listOf(start, end)
        val percent = getPercent() / 100f

        return Color(
            (startC.red + (endC.red - startC.red) * percent).toInt().coerceIn(0, 255),
            (startC.green + (endC.green - startC.green) * percent).toInt().coerceIn(0, 255),
            (startC.green + (endC.green - startC.green) * percent).toInt().coerceIn(0, 255)
        )
    }
}
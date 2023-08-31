package me.odinclient.utils.render.gui.animations.impl

import me.odinclient.utils.render.Color

// TODO: fix it
class ColorAnimation(duration: Long) {

    private val anim = LinearAnimation<Int>(duration) // temporary fix to weird colors

    fun start(bypass: Boolean = false): Boolean {
        return anim.start(bypass)
    }

    fun isAnimating(): Boolean {
        return anim.isAnimating()
    }

    fun percent(): Int {
        return anim.getPercent()
    }

    fun get(start: Color, end: Color, reverse: Boolean): Color {
        return Color(
            anim.get(start.r, end.r, reverse),
            anim.get(start.g, end.g, reverse),
            anim.get(start.b, end.b, reverse),
            anim.get(start.a, end.a, reverse) / 255f,
        )
    }

    /*
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

     */
}
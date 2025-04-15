package me.odinmain.utils.ui.clickgui.animations.impl

import me.odinmain.utils.render.Color

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
            anim.get(start.red, end.red, reverse),
            anim.get(start.green, end.green, reverse),
            anim.get(start.blue, end.blue, reverse),
            anim.get(start.alpha, end.alpha, reverse) / 255f,
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
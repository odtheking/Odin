package me.odinclient.utils.gui.animations

import kotlin.math.pow

class EaseInOut(duration: Long): Animation<Float>(duration) {

    override fun getValue(start: Float, end: Float, reverse: Boolean): Float {
        val startVal = if (reverse) end else start
        val endVal = if (reverse) start else end
        return startVal + (endVal - startVal) * easeInOutCubic(getPercent() / 100f)
    }

    private fun easeInOutCubic(x: Float): Float =
        if (x < 0.5) { 4 * x * x * x } else { 1 - (-2 * x + 2).pow(3) / 2 }
}
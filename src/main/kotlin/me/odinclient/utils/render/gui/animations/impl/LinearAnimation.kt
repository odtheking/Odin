package me.odinclient.utils.render.gui.animations.impl

import me.odinclient.utils.render.gui.animations.Animation

class LinearAnimation(duration: Long): Animation<Float>(duration) {

    override fun get(start: Float, end: Float, reverse: Boolean): Float {
        val startVal = if (reverse) end else start
        val endVal = if (reverse) start else end

        if (!isAnimating()) return if (reverse) start else end
        return startVal + (endVal - startVal) * (getPercent() / 100f)
    }
}
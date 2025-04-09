package me.odinmain.utils.ui.clickgui.animations.impl

import me.odinmain.utils.ui.clickgui.animations.Animation

class EaseOutAnimation(duration: Long): Animation<Float>(duration) {

    override fun get(start: Float, end: Float, reverse: Boolean): Float {
        val startVal = if (reverse) end else start
        val endVal = if (reverse) start else end
        if (!isAnimating()) return endVal
        return startVal + (endVal - startVal) * easeOutQuad()
    }

    private fun easeOutQuad(): Float {
        val percent = getPercent() / 100f
        return 1 - (1 - percent) * (1 - percent)
    }
}
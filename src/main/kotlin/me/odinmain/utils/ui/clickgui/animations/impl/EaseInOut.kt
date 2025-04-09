package me.odinmain.utils.ui.clickgui.animations.impl

import me.odinmain.utils.ui.clickgui.animations.Animation
import kotlin.math.pow

class EaseInOut(duration: Long): Animation<Float>(duration) {
    override fun get(start: Float, end: Float, reverse: Boolean): Float {
        if (!isAnimating()) return if (reverse) start else end
        return if (reverse) end + (start - end) * easeInOutCubic() else start + (end - start) * easeInOutCubic()
    }

    private fun easeInOutCubic(): Float {
        val x = getPercent() / 100f
        return if (x < 0.5) { 4 * x * x * x } else { 1 - (-2 * x + 2).pow(3) / 2 }
    }
}
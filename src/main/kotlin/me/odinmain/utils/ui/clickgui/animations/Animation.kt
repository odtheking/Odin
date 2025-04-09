package me.odinmain.utils.ui.clickgui.animations

import me.odinmain.utils.clock.Clock

/**
 * Simple class that calculates a "point" between two values and a percentage.
 * @author Stivais
 */
abstract class Animation<T>(private var duration: Long) {

    private var animating = false
    private val clock = Clock(duration)

    fun start(bypass: Boolean = false): Boolean {
        if (!animating || bypass) {
            animating = true
            clock.update()
            return true
        }
        return false
    }

    fun getPercent(): Int {
        return if (animating) {
            val percent = (clock.getTime() / duration.toDouble() * 100).toInt()
            if (percent > 100) animating = false
            percent
        } else {
            100
        }
    }

    fun isAnimating(): Boolean {
        return animating
    }

    abstract fun get(start: T, end: T, reverse: Boolean = false): T
}
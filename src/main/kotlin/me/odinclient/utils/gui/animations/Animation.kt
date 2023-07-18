package me.odinclient.utils.gui.animations

abstract class Animation<T>(private var duration: Long) {

    private var animating = false
    private var startTime = 0L

    fun start(bypass: Boolean = false): Boolean {
        if (!animating || bypass) {
            animating = true
            startTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    fun getPercent(): Int {
        return if (animating) {
            val percent = ((System.currentTimeMillis() - startTime) / duration.toDouble() * 100).toInt()
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
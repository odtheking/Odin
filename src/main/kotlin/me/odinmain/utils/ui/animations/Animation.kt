package me.odinmain.utils.ui.animations

abstract class Animation<T>(private val duration: Long) {

    private var startTime: Long = 0L
    private var animating = false
    private var reversed = false

    fun start() {
        val currentTime = System.currentTimeMillis()

        if (!animating) {
            animating = true
            reversed = false
            startTime = currentTime
            return
        }

        val percent = ((currentTime - startTime) / duration.toFloat()).coerceIn(0f, 1f)
        reversed = !reversed
        startTime = currentTime - ((1f - percent) * duration).toLong()
        return
    }

    fun getPercent(): Float {
        if (!animating) return 100f

        val percent = ((System.currentTimeMillis() - startTime) / duration.toFloat() * 100f)
        if (percent >= 100f) {
            animating = false
            return 100f
        }
        return percent.coerceAtMost(100f)
    }

    fun isAnimating(): Boolean = animating

    abstract fun get(start: T, end: T, reverse: Boolean = false): T
}
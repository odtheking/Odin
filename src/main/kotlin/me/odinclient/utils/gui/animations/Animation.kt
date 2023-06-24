package me.odinclient.utils.gui.animations

abstract class Animation<T>(private var duration: Long) {
    private var state = AnimationState.FINISHED
    private var startTime = 0L

    fun start(bypass: Boolean = false): Boolean {
        if (state != AnimationState.ANIMATING || bypass) {
            state = AnimationState.ANIMATING
            startTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    fun getPercent(): Int {
        return when (state) {
            AnimationState.IDLE -> 0
            AnimationState.ANIMATING -> {
                val percent = (((System.currentTimeMillis() - startTime) / duration.toDouble()) * 100).toInt()
                if (percent > 100) state = AnimationState.FINISHED
                percent
            }

            AnimationState.FINISHED -> 100
        }
    }

    abstract fun getValue(start: T, end: T, reverse: Boolean = false): T

    enum class AnimationState {
        IDLE, ANIMATING, FINISHED
    }
}
package com.odtheking.odin.utils.ui.animations

class Tween(private val duration: Long, private val easing: Easing = Easing.LINEAR) {

    private var from = 0f
    private var to = 0f

    private var endTime = 0L
    private var generation = Animations.UNSETTLED

    val value: Float
        get() {
            val remaining = endTime - System.currentTimeMillis()
            if (remaining <= 0) return to
            return from + (to - from) * easing.at(1f - remaining.toFloat() / duration)
        }

    fun target(target: Float) {
        if (generation != Animations.generation) {
            generation = Animations.generation
            return snap(target)
        }

        if (target == to) return
        from = value
        to = target
        endTime = System.currentTimeMillis() + duration
    }

    fun snap(target: Float) {
        from = target
        to = target
        endTime = 0L
    }
}
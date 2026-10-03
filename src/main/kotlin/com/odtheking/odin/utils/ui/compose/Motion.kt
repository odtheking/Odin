package com.odtheking.odin.utils.ui.compose

import com.odtheking.odin.utils.ui.animations.Easing

internal object Motion {
    var frameNanos = 0L
    var settling = false
}

open class Animatable(initial: Float) {
    var target = initial
        private set

    private var from = initial
    private var start = 0L
    private var duration = 0L
    private var easing = Easing.LINEAR

    open val value: Float get() = current()

    private fun current(): Float {
        val elapsed = Motion.frameNanos - start
        if (elapsed >= duration) return target
        return from + (target - from) * easing.at(elapsed.toFloat() / duration)
    }

    fun snapTo(target: Float) {
        this.target = target
        duration = 0L
    }

    fun animateTo(target: Float, durationMillis: Int, easing: Easing = Easing.LINEAR) {
        if (target == this.target) return
        if (Motion.settling) return snapTo(target)
        from = current()
        this.target = target
        this.easing = easing
        start = Motion.frameNanos
        duration = durationMillis * 1_000_000L
    }
}
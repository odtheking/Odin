package com.odtheking.odin.utils.ui.animations

import kotlin.math.roundToInt

class Fade(
    private val duration: Long,
    private val easing: Easing = Easing.LINEAR,
    initial: Boolean = false,
    private val settles: Boolean = true
) {
    private var state = initial
    private var endTime = 0L
    private var generation = Animations.UNSETTLED

    val isAnimating: Boolean get() = System.currentTimeMillis() < endTime

    val current: Boolean get() = state

    fun toggle(): Float = progress(!state)
    fun progress(): Float = progress(state)

    fun progress(state: Boolean): Float {
        val now = System.currentTimeMillis()

        if (settles && generation != Animations.generation) {
            generation = Animations.generation
            this.state = state
            endTime = 0L
            return if (state) 1f else 0f
        }

        if (state != this.state) {
            this.state = state
            endTime = now + duration - (endTime - now).coerceIn(0, duration)
        }

        val remaining = endTime - now
        if (remaining <= 0) return if (state) 1f else 0f

        val eased = easing.at(1f - remaining.toFloat() / duration)
        return if (state) eased else 1f - eased
    }

    fun lerp(state: Boolean, from: Float, to: Float): Float = from + (to - from) * progress(state)
    fun lerp(state: Boolean, from: Int, to: Int): Int = from + ((to - from) * progress(state)).roundToInt()
    fun lerp(from: Int, to: Int): Int = lerp(state, from, to)
}
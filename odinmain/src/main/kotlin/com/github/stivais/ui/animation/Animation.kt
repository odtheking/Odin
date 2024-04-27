package com.github.stivais.ui.animation

class Animation(private var duration: Float, val type: Animations, var from: Float = 0f, var to: Float = 1f) {

    private var time: Long = System.nanoTime()

    var finished: Boolean = false

    fun get(): Float {
        val percent = ((System.nanoTime() - time) / duration)
        finished = percent >= 1f
        return (if (finished) to else from + (to - from) * type.getValue(percent))
    }

    override fun toString(): String = "Animation(duration=$duration)"
}
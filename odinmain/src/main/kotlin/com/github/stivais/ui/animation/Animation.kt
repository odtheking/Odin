package com.github.stivais.ui.animation

class Animation(
    private var duration: Float,
    val type: Animations,
    var from: Float = 0f,
    var to: Float = 1f,
) {

    private var time: Long = System.nanoTime()

    private var onFinish: (() -> Unit)? = null

    var finished: Boolean = false
        set(value) {
            if (value) onFinish?.invoke()
            field = value
        }

    fun get(): Float {
        val percent = ((System.nanoTime() - time) / duration)
        if (percent >= 1f) {
            finished = true
            onFinish?.invoke()
        }
        return (if (finished) to else from + (to - from) * type.getValue(percent))
    }

    infix fun onFinish(block: () -> Unit): Animation {
        onFinish = block
        return this
    }

    override fun toString(): String = "Animation(duration=$duration)"
}
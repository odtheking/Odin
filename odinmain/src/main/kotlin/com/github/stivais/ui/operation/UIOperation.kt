package com.github.stivais.ui.operation

import com.github.stivais.ui.animation.Animation

fun interface UIOperation {
    fun run(): Boolean
}

class AnimationOperation(val animation: Animation, val block: (percent: Float) -> Unit) : UIOperation {

    override fun run(): Boolean {
        block(animation.get())
        return animation.finished
    }
}
package com.github.stivais.ui.animation

sealed interface Animating {

    fun animate(duration: Float, type: Animations): Animation?

    interface Swapping : Animating {
        fun swap()
    }
}
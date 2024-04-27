package com.github.stivais.ui.animation

import kotlin.math.pow

private interface Strategy {
    fun getValue(percent: Float): Float
}

// todo: add more
enum class Animations : Strategy {
    Linear {
        override fun getValue(percent: Float): Float = percent
    },
    EaseInQuint {
        override fun getValue(percent: Float): Float = percent * percent * percent * percent * percent
    },
    EaseOutQuint {
        override fun getValue(percent: Float): Float = 1 - (1 - percent).pow(5f)
    },
    EaseInOutQuint {
        override fun getValue(percent: Float): Float {
            return if (percent < 0.5f) 16f * percent * percent * percent * percent * percent
            else 1 - (-2 * percent + 2).pow(5f) / 2f
        }
    };
}
@file:Suppress("UNUSED")

package com.github.stivais.ui.animation

import kotlin.math.pow

private interface Strategy {
    fun getValue(percent: Float): Float
}

/**
 * A bunch of animations commonly used in UI
 *
 * Animations taken from [https://easings.net/](https://easings.net/)
 *
 * @see Animation
 */
enum class Animations : Strategy {
    Linear {
        override fun getValue(percent: Float): Float = percent
    },
    EaseInQuad {
        override fun getValue(percent: Float): Float = percent * percent
    },
    EaseOutQuad {
        override fun getValue(percent: Float): Float = 1 - (1 - percent) * (1 - percent)
    },
    EaseInOutQuad {
        override fun getValue(percent: Float): Float {
            return if (percent < 0.5) 2 * percent * percent
            else 1 - (-2 * percent + 2).pow(2f) / 2
        }
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
    },
    EaseInBack {
        override fun getValue(percent: Float): Float {
            val c1 = 1.70158f
            val c3 = c1 + 1
            return c3 * percent * percent * percent - c1 * percent * percent
        }
    },
}
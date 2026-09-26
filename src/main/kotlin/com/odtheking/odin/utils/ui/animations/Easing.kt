package com.odtheking.odin.utils.ui.animations

import kotlin.math.pow

enum class Easing {
    LINEAR {
        override fun at(progress: Float): Float = progress
    },

    EASE_IN_OUT {
        override fun at(progress: Float): Float =
            if (progress < 0.5f) 4f * progress * progress * progress
            else 1f - (-2f * progress + 2f).pow(3) / 2f
    };

    abstract fun at(progress: Float): Float
}
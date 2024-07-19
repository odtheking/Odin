package com.github.stivais.ui.utils

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.color
import com.github.stivais.ui.constraints.Constraint
import com.github.stivais.ui.constraints.measurements.Animatable

@JvmName("radiiThis")
fun Number.radii() = radius(this)

/**
 * Takes 4 numbers, and creates a [FloatArray] with those values
 *
 * It is used for rounded elements
 */
fun radius(tl: Number = 0f, tr: Number = 0f, bl: Number = 0f, br: Number = 0f) = floatArrayOf(tl.toFloat(), bl.toFloat(), br.toFloat(), tr.toFloat())

/**
 * Takes 1 numbers, and converts it into a [FloatArray] with a size of 4
 *
 * It is used for rounded elements
 */
fun radius(all: Number): FloatArray {
    val value = all.toFloat()
    return floatArrayOf(value, value, value, value)
}

/**
 * Convenient function to check and animate a [Color] if you know it is a [Animating Color][Color.Animated]
 *
 * If the color isn't [animating][Color.Animated], it will not do anything
 *
 * @param duration How long the animation lasts (in nanoseconds)
 * @param type The type of animation done
 */
fun Color.animate(duration: Number, type: Animations = Animations.Linear) {
    if (this is Color.Animated) {
        animate(duration.toFloat(), type)
    }
}

/**
 * Convenient function to check and animate a [Constraint] if you know it is [animating][Animatable]
 *
 * If the constraint isn't [animating][Animatable], it will not do anything
 *
 * @param duration How long the animation lasts (in nanoseconds)
 * @param type The type of animation done
 */
fun Constraint.animate(duration: Number, type: Animations = Animations.Linear) {
    if (this is Animatable) {
        animate(duration.toFloat(), type)
    }
}

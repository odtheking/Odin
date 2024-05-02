package com.github.stivais.ui.utils

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraint
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.minus
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.events.Mouse
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.events.onMouseMove
import com.github.stivais.ui.events.onRelease

/**
 * Takes 4 numbers, and creates a [FloatArray] with those values
 *
 * It is used for rounded elements
 */
fun radii(tl: Number = 0f, tr: Number = 0f, bl: Number = 0f, br: Number = 0f) = floatArrayOf(tl.toFloat(), bl.toFloat(), br.toFloat(), tr.toFloat())

/**
 * Takes 1 numbers, and converts it into a [FloatArray] with a size of 4
 *
 * It is used for rounded elements
 */
fun radii(all: Number): FloatArray {
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

fun color(r: Int, g: Int, b: Int, alpha: Float = 1f) = Color.RGB(r, g, b, alpha)

fun color(h: Float, s: Float, b: Float, alpha: Float = 1f) = Color.HSB(h, s, b, alpha)

fun color(from: Color, to: Color, swap: Boolean = false) = Color.Animated(from, to, swap)

val Number.seconds
    get() = this.toFloat() * 1_000_000_000


// todo: cleanup
/**
 * Function that allows elements to be drag and dropped
 *
 * @param acceptsEvent If the events shouldn't be allowed to pass on
 * @param target The element to move when dragged
 */
fun <E : Element> E.draggable(acceptsEvent: Boolean = true, target: Element = this): E {
    val px: Pixel = 0.px
    val py: Pixel = 0.px
    // note: if parent is Bounding, it can cause issues
    afterInitialization {
        px.pixels = target.internalX
        py.pixels = target.internalY
        target.constraints.x = px
        target.constraints.y = py
    }
    var pressed = false
    var x = 0f
    var y = 0f
    onClick(0) {
        pressed = true
        x = ui.mx - this@draggable.x
        y = ui.my - this@draggable.y
        acceptsEvent
    }
    onMouseMove {
        if (pressed) {
            px.pixels = ui.mx - x
            py.pixels = ui.my - y
        }
        acceptsEvent
    }
    onRelease(0) {
        pressed = false
    }
    return this
}

/**
 * Sets the element to be focused on click
 */
fun <E : Element> E.focuses(): E {
    onClick(0) {
        ui.focus(this@focuses)
        true
    }
    return this
}

// todo: rework scrolling, as this is really suboptimal
fun <E : Element> E.scrollable(duration: Float, target: Element, min: Float = 0f): E {
    var s = 0f
    val anim = Animatable.Raw(0f)
    target.constraints.apply {
        y = (y - anim)
    }
    registerEvent(Mouse.Scrolled(0f)) {
        s = (s - (this as Mouse.Scrolled).amount * 16).coerceIn(min, target.height)
        anim.animate(s, duration)
        true
    }
    return this
}
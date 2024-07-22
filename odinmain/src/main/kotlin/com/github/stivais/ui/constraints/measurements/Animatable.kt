package com.github.stivais.ui.constraints.measurements

import com.github.stivais.ui.animation.Animating
import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.elements.Element

/**
 * # Animatable
 *
 * The animatable constraint allows you to easily implement animations that'll appear exactly how you wanted them to.
 *
 * It works by animating between 2 different [Constraints][Constraint] provided, swapping between each when animated.
 *
 * If you need an animating constraint, where you need to animate to any point, look at [Animatable.Raw]
 *
 * @see Animatable.Raw
 */
class Animatable(var from: Constraint, var to: Constraint): Measurement, Animating.Swapping {

    constructor(from: Constraint, to: Constraint, swapIf: Boolean) : this(from, to) {
        if (swapIf) {
            swap()
        }
    }

    /**
     * Current animation for this [Animatable]
     *
     * If this is null, that means it isn't animating
     */
    var animation: Animation? = null
        private set

    private var current: Float = 0f

    // used to effectively to smoothly swap animations
    private var before: Float? = null

    override fun get(element: Element, type: Type): Float {
        if (animation != null) {
            element.redraw = true
            val progress = animation!!.get()
            val from = before ?: from.get(element, type)
            current = from + (to.get(element, type) - from) * progress

            if (animation!!.finished) {
                animation = null
                before = null
                swap()
            }
            return current
        }
        return from.get(element, type)
    }

    override fun animate(duration: Float, type: Animations): Animation? {
        if (duration == 0f) {
            swap()
        } else {
            if (animation != null) {
                before = current
                swap()
                animation = Animation(duration * animation!!.get(), type)
            } else {
                animation = Animation(duration, type)
            }
        }
        return animation
    }

    override fun swap() {
        val temp = to
        to = from
        from = temp
    }

    override fun reliesOnChild(): Boolean {
        return from.reliesOnChild() || to.reliesOnChild()
    }

    /**
     * # Animatable.Raw
     *
     * This constraint allows you to animate to any points, used if you need to smoothly move something to a certain point.
     *
     * Note: The result could be messy
     */
    class Raw(start: Float) : Measurement {

        var current: Float = start

        private var animation: Animation? = null

        fun animate(to: Float, duration: Float, type: Animations = Animations.Linear) {
            if (duration != 0f) animation = Animation(duration, type, animation?.get() ?: current, to) else current = to
        }

        fun to(to: Float) = if (animation != null) animation!!.to = to else current = to

        override fun get(element: Element, type: Type): Float {
            if (animation != null) {
                element.redraw = true
                val result = animation!!.get()
                if (animation!!.finished) {
                    animation = null
                    current = result
                }
                return result
            }
            return current
        }
    }
}

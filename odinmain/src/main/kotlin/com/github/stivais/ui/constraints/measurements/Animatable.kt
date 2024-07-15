package com.github.stivais.ui.constraints.measurements

import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.Constraint
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Animatable(var from: Constraint, var to: Constraint): Measurement {

    constructor(from: Constraint, to: Constraint, swap: Boolean) : this(from, to) {
        if (swap) {
            swap()
        }
    }

    var animation: Animation? = null

    private var current: Float = 0f

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

    fun animate(duration: Float, type: Animations): Animation? {
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

    fun swap() {
        val temp = to
        to = from
        from = temp
    }

    override fun reliesOnChild(): Boolean {
        return from.reliesOnChild() || to.reliesOnChild()
    }

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

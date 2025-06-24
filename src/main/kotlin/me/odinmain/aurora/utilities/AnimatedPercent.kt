package me.odinmain.aurora.utilities

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.components.Component
import com.github.stivais.aurora.measurements.Measurement
import com.github.stivais.aurora.measurements.impl.Percent
import com.github.stivais.aurora.utils.Timing

/**
 * # AnimatedPercent
 *
 * Measurement, which allows you to animate a [percent measurement][Percent].
 *
 * To animate it, start an animation with [animate] and set a location with [to].
 */
class AnimatedPercent(private val percent: Percent) : Measurement.Position, Measurement.Size {

    private var animation: Animation? = null

    private var from: Float = 0f
    private var to: Float = 0f

    /**
     * Starts an animation with provided [Timing] and [formula][Animation.Formula]
     */
    fun animate(duration: Timing, formula: Animation.Formula) {
        from = percent.amount
        animation = Animation(duration, formula)
    }

    /**
     * Sets the location to animate to.
     *
     * If there is no active animation, it simply sets the value directly.
     *
     * @param value percent value to animate to. ranging from 0-1.
     */
    fun to(value: Float) {
        if (animation != null) to = value else percent.amount = value
    }

    private fun update(component: Component) {
        if (animation != null) {
            percent.amount = from + (to - from) * animation!!.get()
            if (animation!!.finished) animation = null
            component.redraw()
        }
    }

    override fun calculateX(component: Component): Float {
        update(component)
        return percent.calculateX(component)
    }

    override fun calculateY(component: Component): Float {
        update(component)
        return percent.calculateY(component)

    }

    override fun calculateWidth(component: Component): Float {
        update(component)
        return percent.calculateWidth(component)
    }

    override fun calculateHeight(component: Component): Float {
        update(component)
        return percent.calculateHeight(component)
    }
}
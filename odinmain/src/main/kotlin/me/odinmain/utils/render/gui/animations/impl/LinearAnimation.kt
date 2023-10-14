package me.odinmain.utils.render.gui.animations.impl

import me.odinmain.utils.minus
import me.odinmain.utils.plus
import me.odinmain.utils.render.gui.animations.Animation
import me.odinmain.utils.times

class LinearAnimation<E>(duration: Long): Animation<E>(duration) where E : Number, E: Comparable<E> {

    // x + 10f (x + 220f - x + 10f) * sat

    override fun get(start: E, end: E, reverse: Boolean): E {
        val startVal = if (reverse) end else start
        val endVal = if (reverse) start else end

        if (!isAnimating()) return if (reverse) start else end
        return (startVal + (endVal - startVal) * (getPercent() / 100f)) as E
    }
}
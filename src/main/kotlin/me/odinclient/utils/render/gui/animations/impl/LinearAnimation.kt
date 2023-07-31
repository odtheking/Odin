package me.odinclient.utils.render.gui.animations.impl

import me.odinclient.utils.Utils.minus
import me.odinclient.utils.Utils.plus
import me.odinclient.utils.Utils.times
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.animations.Animation

class LinearAnimation<E>(duration: Long): Animation<E>(duration) where E : Number, E: Comparable<E> {

    // x + 10f (x + 220f - x + 10f) * sat

    override fun get(start: E, end: E, reverse: Boolean): E {
        val startVal = if (reverse) end else start
        val endVal = if (reverse) start else end

        if (!isAnimating()) return if (reverse) start else end
        return (startVal + (endVal - startVal) * (getPercent() / 100f)) as E
    }
}
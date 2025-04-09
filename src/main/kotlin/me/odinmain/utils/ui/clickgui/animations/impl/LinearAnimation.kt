package me.odinmain.utils.ui.clickgui.animations.impl

import me.odinmain.utils.ui.clickgui.animations.Animation

class LinearAnimation<E>(duration: Long): Animation<E>(duration) where E : Number, E: Comparable<E> {

    @Suppress("UNCHECKED_CAST")
    override fun get(start: E, end: E, reverse: Boolean): E {
        val startVal = if (reverse) end.toFloat() else start.toFloat()
        val endVal = if (reverse) start.toFloat()  else end.toFloat()

        if (!isAnimating()) return if (reverse) start else end
        return (startVal + (endVal - startVal) * (getPercent() / 100f)) as E
    }
}

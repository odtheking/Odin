package me.odinmain.utils.ui.animations

class LinearAnimation<E>(duration: Long): Animation<E>(duration) where E : Number, E: Comparable<E> {

    @Suppress("UNCHECKED_CAST")
    override fun get(start: E, end: E, reverse: Boolean): E {
        val startVal = if (reverse) end else start
        val endVal = if (reverse) start else end

        if (!isAnimating()) return if (reverse) start else end
        return (startVal.toFloat() + (endVal.toFloat() - startVal.toFloat()) * (getPercent() / 100f)) as E
    }
}
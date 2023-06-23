package me.odinclient.utils.gui.animations

class EaseOutAnimation(duration: Long): Animation<Float>(duration) {
    override fun getValue(start: Float, end: Float, reverse: Boolean): Float {
        val startVal = if (reverse) end else start
        val endVal = if (reverse) start else end
        return startVal + (endVal - startVal) * easeOutQuad(getPercent() / 100f)
    }

    private fun easeOutQuad(percent: Float): Float =
        1 - (1 - percent) * (1 - percent)
}
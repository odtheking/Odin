package me.odinclient.ui.clickgui.util


import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.animations.impl.LinearAnimation

class HoverHandler(val delay: Long) {

    private val anim = LinearAnimation<Int>(delay)

    private var hoverStartTime: Long? = null
    private var hasStarted = false

    val alpha: Float
        get() {
            return hoverStartTime?.let { (System.currentTimeMillis() - it) / 750f }?.coerceIn(0f, 0.3f) ?: 0f
        }

    fun percent(): Int {
        if (!hasStarted) return 100 - anim.getPercent()
        return anim.getPercent()
    }

    fun handle(x: Float, y: Float, w: Float, h: Float) {
        if (isAreaHovered(x, y, w, h)) {
            if (hoverStartTime == null) hoverStartTime = System.currentTimeMillis()

            if (System.currentTimeMillis() - hoverStartTime!! >= startDelay && !hasStarted) {
                anim.start()
                hasStarted = true
            }
        } else {
            hoverStartTime = null
            if (hasStarted) {
                anim.start()
                isHovered = false
            }
        }
    }
}
package me.odinclient.ui.clickgui.util

import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.animations.impl.LinearAnimation

class HoverHandler(val delay: Long) {

    val anim = LinearAnimation<Int>(delay)

    private var isHovered = false

    fun percent(): Int {
        return anim.get(0, 100, !isHovered)
    }

    fun handle(x: Float, y: Float, w: Float, h: Float) {
        if (isAreaHovered(x, y, w, h)) {
            if (!isHovered) {
                isHovered = true
                anim.start()
            }
        } else {
            if (isHovered) {
                anim.start(true)
                isHovered = false
            }
        }
    }
}
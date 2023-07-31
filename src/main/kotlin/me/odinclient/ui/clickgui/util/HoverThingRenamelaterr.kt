package me.odinclient.ui.clickgui.util

import me.odinclient.utils.render.gui.MouseUtils

class HoverThingRenamelaterr {

    var timeHovered = 0L
        private set

    private var startTime: Long? = null

    fun handle(x: Float, y: Float, w: Float, h: Float) {
        if (MouseUtils.isAreaHovered(x, y, w, h)) {
            if (startTime == null) startTime = System.currentTimeMillis()
            timeHovered = System.currentTimeMillis() - startTime!!
        } else {
            timeHovered = 0L
            startTime = null
        }
    }
}
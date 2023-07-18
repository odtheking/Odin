package me.odinclient.ui.clickgui.util

import cc.polyfrost.oneconfig.libs.universal.UMouse

object MouseUtils {

    val mouseX
        get() = UMouse.Scaled.x.toFloat() * 2f

    val mouseY
        get() = UMouse.Scaled.y.toFloat() * 2f

    fun isAreaHovered(x: Float, y: Float, w: Float, h: Float): Boolean {
        return mouseX in x..x + w && mouseY in y..y + h
    }

    fun isAreaHovered(x: Float, y: Float, w: Float): Boolean {
        return mouseX in x..x + w && mouseY >= y
    }
}
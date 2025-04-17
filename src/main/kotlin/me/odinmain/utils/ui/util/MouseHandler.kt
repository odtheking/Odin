package me.odinmain.utils.ui.util

/**
 * Edited verison of OneConfig InputHandler to have a translation option cuz why not
 * Add more stuff here and maybe merge with the nanoVG stuff so its nice and clean
 * // TODO: Instead of a class implement methods
 */
class MouseHandler {
    private var scaleX = 1f
    private var scaleY = 1f
    private var tX = 0f
    private var tY = 0f

    fun scale(scaleX: Float, scaleY: Float) {
        this.scaleX = scaleX
        this.scaleY = scaleY
    }

    fun translate(x: Float, y: Float) {
        tX = x
        tY = y
    }

    val mouseX get() =
        (MouseUtils.mouseX - tX) / scaleX

    val mouseY get() =
        (MouseUtils.mouseY - tY) / scaleY

    fun isAreaHovered(x: Float, y: Float, width: Float, height: Float): Boolean =
        mouseX > x && mouseY > y && mouseX < x + width && mouseY < y + height
}
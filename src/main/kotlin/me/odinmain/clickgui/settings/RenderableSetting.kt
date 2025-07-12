package me.odinmain.clickgui.settings

import me.odinmain.clickgui.ClickGUI
import me.odinmain.clickgui.Panel
import me.odinmain.utils.ui.HoverHandler
import me.odinmain.utils.ui.isAreaHovered

abstract class RenderableSetting<T>(
    name: String,
    description: String
) : Setting<T>(name, description) {

    private val hoverHandler = HoverHandler(750, 200)
    protected val width = Panel.WIDTH
    protected var lastX = 0f
    protected var lastY = 0f
    var listening = false

    open fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        lastX = x
        lastY = y
        val height = getHeight()
        hoverHandler.handle(x, y, width, height)
        if (hoverHandler.percent() > 0)
            ClickGUI.setDescription(description, x + width + 10f, y, hoverHandler)

        return height
    }

    open fun mouseClicked(mouseX: Float, mouseY: Float, mouseButton: Int): Boolean = false
    open fun mouseReleased(state: Int) {}
    open fun keyTyped(typedChar: Char): Boolean = false
    open fun keyPressed(keyCode: Int): Boolean = false
    open fun getHeight(): Float = Panel.HEIGHT

    open val isHovered get() = isAreaHovered(lastX, lastY, width, getHeight())
}
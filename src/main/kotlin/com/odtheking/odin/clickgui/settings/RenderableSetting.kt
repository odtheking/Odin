package com.odtheking.odin.clickgui.settings

import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.Panel
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

abstract class RenderableSetting<T>(
    name: String,
    description: String
) : Setting<T>(name, description) {

    private val hoverHandler = HoverHandler(750)
    protected val width = Panel.WIDTH
    protected var lastX = 0f
    protected var lastY = 0f
    var listening = false

    open fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        lastX = x
        lastY = y
        val height = getHeight()
        hoverHandler.handle(x, y, width, height, true)
        if (hoverHandler.percent() > 0)
            ClickGUI.setDescription(description, x + width + 10f, y, hoverHandler)

        return height
    }

    open fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean = false
    open fun mouseReleased(click: MouseButtonEvent) {}
    open fun keyTyped(input: CharacterEvent): Boolean = false
    open fun keyPressed(input: KeyEvent): Boolean = false
    open fun getHeight(): Float = Panel.HEIGHT

    open val isHovered get() = isAreaHovered(lastX, lastY, width, getHeight(), true)
}
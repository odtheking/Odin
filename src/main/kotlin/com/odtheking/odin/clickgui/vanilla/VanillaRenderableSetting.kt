package com.odtheking.odin.clickgui.vanilla

import com.odtheking.odin.clickgui.settings.Setting
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

abstract class VanillaRenderableSetting<T>(
    name: String,
    description: String
) : Setting<T>(name, description) {

    private val hoverHandler = HoverHandler(750)
    protected val width = Panel.WIDTH
    protected var lastX = 0
    protected var lastY = 0
    var listening = false

    open fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        lastX = x
        lastY = y
        val height = getHeight()
        hoverHandler.handle(x, y, width, height)
        if (hoverHandler.percent() > 0)
            VanillaGUI.setDescription(description, (x + width + 10), y, hoverHandler)

        return height
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean = false
    open fun mouseReleased(mouseX: Int, mouseY: Int, click: MouseButtonEvent) {}
    open fun keyTyped(input: CharacterEvent): Boolean = false
    open fun keyPressed(input: KeyEvent): Boolean = false
    open fun getHeight(): Int = Panel.HEIGHT

    open val isHovered get() = isAreaHovered(lastX, lastY, width, getHeight())
}
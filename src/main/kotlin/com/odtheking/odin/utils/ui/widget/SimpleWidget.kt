package com.odtheking.odin.utils.ui.widget

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

class SimpleWidget(
    var x: Int = 0,
    var y: Int = 0,
    val width: Int = 0,
    val height: Int = 0
) {
    var visible: Boolean = true
    var active: Boolean = true
    var isHovered: Boolean = false
        private set

    private var onRenderCallback: GuiGraphics.(Int, Int, Int, Int) -> Unit = { _, _, _, _ -> }
    private var onKeyPressCallback: (KeyEvent) -> Boolean = { false }
    private var onClickCallback: (MouseButtonEvent, Boolean) -> Boolean = { _, _ -> false }
    private var onMouseReleaseCallback: (MouseButtonEvent) -> Unit = {}

    fun onRender(callback: GuiGraphics.(Int, Int, Int, Int) -> Unit) {
        onRenderCallback = callback
    }

    fun onKeyPress(callback: (KeyEvent) -> Boolean) {
        onKeyPressCallback = callback
    }

    fun onClick(callback: (MouseButtonEvent, Boolean) -> Boolean) {
        onClickCallback = callback
    }

    fun onMouseRelease(callback: (MouseButtonEvent) -> Unit) {
        onMouseReleaseCallback = callback
    }

    fun render(guiGraphics: GuiGraphics) {
        if (!visible) return
        guiGraphics.onRenderCallback(x, y, width, height)
    }

    fun keyPressed(keyEvent: KeyEvent): Boolean = onKeyPressCallback(keyEvent)

    fun mouseClicked(mouseButtonEvent: MouseButtonEvent, doubled: Boolean): Boolean {
        if (!visible || !active) return false
        return onClickCallback(mouseButtonEvent, doubled)
    }

    fun onRelease(mouseButtonEvent: MouseButtonEvent) {
        if (!visible) return
        onMouseReleaseCallback(mouseButtonEvent)
    }

    fun contains(mouseX: Int, mouseY: Int): Boolean =
        mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
}

fun simpleWidget(x: Int = 0, y: Int = 0, width: Int = 0, height: Int = 0, init: SimpleWidget.() -> Unit): SimpleWidget {
    return SimpleWidget(x, y, width, height).apply(init)
}
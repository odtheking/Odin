package com.github.stivais.ui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse

class UIScreen(val ui: UI) : GuiScreen() {

    private var previousWidth: Int = 0
    private var previousHeight: Int = 0

    override fun initGui() {
        ui.initialize()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        ui.eventManager?.apply {
            val mx = Mouse.getX().toFloat()
            val my = mc.displayHeight - Mouse.getY() - 1f

            if (this.mouseX != mx || this.mouseY != my) {
                onMouseMove(mx, my)
            }

            val scroll = Mouse.getEventDWheel()
            if (scroll != 0) {
                onMouseScroll(scroll.toFloat())
            }
        }
        val w = mc.framebuffer.framebufferWidth
        val h = mc.framebuffer.framebufferHeight
        if (w != previousWidth || h != previousHeight) {
            ui.resize(w, h)
            previousWidth = w
            previousHeight = h
        }
        ui.render()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        ui.eventManager?.onMouseClick(button)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Int) {
        ui.eventManager?.onMouseRelease(button)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (ui.eventManager?.onKeyType(typedChar) == true) return
        if (ui.eventManager?.onKeycodePressed(keyCode) == true) return
        super.keyTyped(typedChar, keyCode)
    }

//    no key released because 1.8.9 doesn't have it and I don't want to manually recreate it
//    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
//        if (ui.eventManager?.onKeyReleased(keyCode) == true) {
//            return true
//        }
//        return super.keyPressed(keyCode, scanCode, modifiers)
//    }

    override fun onResize(mcIn: Minecraft?, w: Int, h: Int) {
        ui.resize(mc.framebuffer.framebufferWidth, mc.framebuffer.framebufferHeight)
        super.onResize(mcIn, w, h)
    }

    override fun doesGuiPauseGame(): Boolean = false
}
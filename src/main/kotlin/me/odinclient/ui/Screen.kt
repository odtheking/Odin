package me.odinclient.ui

import me.odinclient.utils.render.gui.nvg.NVG
import me.odinclient.utils.render.gui.nvg.drawNVG
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse

abstract class Screen : GuiScreen() {

    abstract fun draw(nvg: NVG)

    open fun onScroll(amount: Int) {}

    final override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawNVG {
            draw(this)
        }
    }

    final override fun handleMouseInput() {
        super.handleMouseInput()
        val scrollEvent = Mouse.getEventDWheel()
        if (scrollEvent != 0) {
            onScroll(scrollEvent)
        }
    }

    final override fun doesGuiPauseGame(): Boolean {
        return false
    }
}
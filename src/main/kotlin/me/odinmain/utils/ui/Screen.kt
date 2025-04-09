package me.odinmain.utils.ui


import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse


abstract class Screen : GuiScreen() {

    abstract fun draw()

    open fun onScroll(amount: Int) {}

    final override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        draw()
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
package com.odtheking.odin.clickgui

import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.ui.compose.UiHost
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

abstract class OdinScreen(title: Component) : Screen(title) {

    protected abstract val ui: UiHost

    protected open val guiScale: Float get() = 1f

    override fun init() {
        ui.settle()
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean =
        ui.click(event.intoGuiSpace(), doubleClick) || super.mouseClicked(event, doubleClick)

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean =
        ui.drag(event.intoGuiSpace()) || super.mouseDragged(event, dragX, dragY)

    override fun mouseReleased(event: MouseButtonEvent): Boolean =
        ui.release(event.intoGuiSpace()) || super.mouseReleased(event)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean =
        ui.scroll(mouseX / guiScale, mouseY / guiScale, verticalAmount) ||
            super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)

    override fun charTyped(event: CharacterEvent): Boolean = ui.char(event) || super.charTyped(event)

    override fun keyPressed(event: KeyEvent): Boolean = ui.key(event) || super.keyPressed(event)

    override fun onClose() {
        ui.dismiss()
        ModuleManager.saveConfigurations()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean = false

    private fun MouseButtonEvent.intoGuiSpace(): MouseButtonEvent =
        if (guiScale == 1f) this else MouseButtonEvent(x() / guiScale, y() / guiScale, buttonInfo())
}
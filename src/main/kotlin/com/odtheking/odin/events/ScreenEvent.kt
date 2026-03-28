package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

abstract class ScreenEvent(val screen: Screen) : CancellableEvent() {

    class Open(screen: Screen) : ScreenEvent(screen)

    class Close(screen: Screen) : ScreenEvent(screen)

    class MouseClick(screen: Screen, val click: MouseButtonEvent) : ScreenEvent(screen)

    class MouseRelease(screen: Screen, val click: MouseButtonEvent) : ScreenEvent(screen)

    class KeyPress(screen: Screen, val input: KeyEvent) : ScreenEvent(screen)

    class Render(screen: Screen, val guiGraphics: GuiGraphics, val mouseX: Int, val mouseY: Int) : ScreenEvent(screen)
}
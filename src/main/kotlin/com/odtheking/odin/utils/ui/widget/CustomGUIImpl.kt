package com.odtheking.odin.utils.ui.widget

import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import net.minecraft.client.gui.screens.Screen

object CustomGUIImpl {
    private var currentScreen: Screen? = null
    private val widgets = mutableListOf<SimpleWidget>()

    init {
        on<ScreenEvent.Render> (EventPriority.HIGHEST) {
            updateScreenIfChanged(screen)
            if (currentScreen == null) return@on
            widgets.forEach { it.render(guiGraphics) }
            if (widgets.isNotEmpty()) cancel()
        }

        on<ScreenEvent.MouseClick> (EventPriority.HIGHEST)  {
            updateScreenIfChanged(screen)
            if (currentScreen == null) return@on
            val consumed = widgets
                .asReversed()
                .any { widget ->
                    widget.active && widget.visible && widget.contains(click.x().toInt(), click.y().toInt()) && widget.mouseClicked(click, doubled)
                }
            if (consumed) cancel()
        }

        on<ScreenEvent.MouseRelease> (EventPriority.HIGHEST)  {
            updateScreenIfChanged(screen)
            if (currentScreen == null) return@on
            val handled = widgets
                .asReversed()
                .firstOrNull { widget -> widget.active && widget.visible && widget.contains(click.x().toInt(), click.y().toInt()) }
                ?: return@on

            handled.onRelease(click)
            cancel()
        }

        on<ScreenEvent.KeyPress> (EventPriority.HIGHEST) {
            updateScreenIfChanged(screen)
            if (currentScreen == null) return@on
            val consumed = widgets
                .asReversed()
                .any { widget -> widget.visible && widget.active && widget.keyPressed(input) }
            if (consumed) cancel()
        }
    }

    private fun updateScreenIfChanged(screen: Screen) {
        if (currentScreen != screen) {
            currentScreen = screen
            widgets.clear()
        }
    }

    fun register(screen: Screen, widget: SimpleWidget): SimpleWidget {
        updateScreenIfChanged(screen)
        widgets.add(widget)
        return widget
    }

    fun clear(screen: Screen? = currentScreen) {
        if (screen == null) return
        if (currentScreen != screen) currentScreen = screen
        widgets.clear()
    }
}
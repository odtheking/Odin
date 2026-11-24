package com.odtheking.odin.utils.ui.widget

import androidx.compose.runtime.Composable
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.ScreenCloseEvent
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.core.EventPriority
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.ui.compose.UiHost
import net.minecraft.client.input.KeyEvent

object CustomGUIImpl {

    class Overlay internal constructor(
        val enabled: () -> Boolean,
        private val scale: () -> Float,
        private val beforeRender: ScreenEvent.Render.() -> Unit,
        private val onKey: Overlay.(KeyEvent) -> Boolean,
        content: @Composable () -> Unit,
    ) {
        val ui by lazy { UiHost(content) }

        private var active = false

        fun hovered(): Any? = ui.hitTest(mc.mouseHandler.getScaledXPos(mc.window), mc.mouseHandler.getScaledYPos(mc.window))

        internal fun update(): Boolean {
            val enabled = enabled()
            if (enabled && !active) ui.settle()
            if (!enabled) reset()
            active = enabled
            return enabled
        }

        internal fun reset() {
            if (active) ui.dismiss()
            active = false
        }

        internal fun render(event: ScreenEvent.Render) {
            event.beforeRender()
            ui.scale = scale()
            ui.render(event.guiGraphics, event.screen.width, event.screen.height, event.mouseX, event.mouseY)
        }

        internal fun click(event: ScreenEvent.MouseClick) {
            ui.click(event.click, false)
        }

        internal fun release(event: ScreenEvent.MouseRelease) {
            ui.release(event.click)
        }

        internal fun key(event: ScreenEvent.KeyPress): Boolean = onKey(event.input)
    }

    private val overlays = ArrayList<Overlay>()

    init {
        on<ScreenEvent.Open> { overlays.forEach(Overlay::reset) }
        on<ScreenCloseEvent> { overlays.forEach(Overlay::reset) }

        on<ScreenEvent.Render> (EventPriority.HIGHEST) {
            var rendered = false
            for (overlay in overlays) if (overlay.update()) {
                overlay.render(this)
                rendered = true
            }
            if (rendered) cancel()
        }

        on<ScreenEvent.MouseClick> (EventPriority.HIGHEST) {
            if (forEachActive { it.click(this) }) cancel()
        }

        on<ScreenEvent.MouseRelease> (EventPriority.HIGHEST) {
            if (forEachActive { it.release(this) }) cancel()
        }

        on<ScreenEvent.KeyPress> (EventPriority.HIGHEST) {
            var consumed = false
            forEachActive { if (it.key(this)) consumed = true }
            if (consumed) cancel()
        }
    }

    fun register(
        enabled: () -> Boolean,
        scale: () -> Float = { 1f },
        beforeRender: ScreenEvent.Render.() -> Unit = {},
        onKey: Overlay.(KeyEvent) -> Boolean = { false },
        content: @Composable () -> Unit,
    ): Overlay = Overlay(enabled, scale, beforeRender, onKey, content).also { overlays += it }

    private inline fun forEachActive(action: (Overlay) -> Unit): Boolean {
        var any = false
        for (overlay in overlays) if (overlay.update()) {
            action(overlay)
            any = true
        }
        return any
    }
}
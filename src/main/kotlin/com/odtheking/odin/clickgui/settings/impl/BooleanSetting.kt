package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.widget.Toggle
import com.odtheking.odin.clickgui.widget.drawToggle
import com.odtheking.odin.utils.ui.animations.Fade
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent

class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    desc: String,
) : RenderableSetting<Boolean>(name, desc), Saving {

    override var value: Boolean = default
    var enabled: Boolean by this::value

    private val toggleAnimation = Fade(TOGGLE_DURATION)

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        drawLabel(graphics)
        graphics.drawToggle(x + width - Toggle.WIDTH - RIGHT_PAD, y + height / 2, toggleAnimation.progress(enabled), hover)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        enabled = !enabled
    }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(enabled)

    override fun read(element: JsonElement, gson: Gson) {
        enabled = element.asBoolean
    }

    private companion object {
        const val TOGGLE_DURATION = 200L
        const val RIGHT_PAD = 6
    }
}
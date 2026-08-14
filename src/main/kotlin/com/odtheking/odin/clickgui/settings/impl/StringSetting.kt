package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.widget.rowTextField
import com.odtheking.odin.utils.render.roundedRectOutlined
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class StringSetting(
    name: String,
    override val default: String = "",
    val length: Int = 32,
    desc: String,
    private val placeholder: String
) : RenderableSetting<String>(name, desc, GuiTheme.ROW_HEIGHT + EXTRA_HEIGHT), Saving {

    override var value: String = default
        set(value) {
            if (value.length <= length) field = value
        }

    private val input by lazy {
        rowTextField(GuiTheme.ROW_WIDTH - BOX_INSET * 2 - TEXT_INSET * 2, name, false).apply {
            setMaxLength(length)
            value = this@StringSetting.value
            setResponder { this@StringSetting.value = it }
            if (placeholder.isNotEmpty()) setHint(Component.literal(placeholder).withStyle(EditBox.DEFAULT_HINT_STYLE))
        }
    }

    private var pushed = default

    override fun children(): List<GuiEventListener> = listOf(input)

    override fun place() {
        input.x = x + BOX_INSET + TEXT_INSET
        input.y = GuiTheme.textY(y + height - BOX_HEIGHT, BOX_HEIGHT)

        val text = value
        if (text == pushed) return
        pushed = text
        if (!input.isFocused) input.value = text
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        setFocused(input)
        input.onClick(event, doubleClick)
    }

    override fun release() {
        setFocused(null)
    }

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        drawLabel(graphics)

        val boxX = x + BOX_INSET
        val boxY = y + height - BOX_HEIGHT
        val boxRight = boxX + width - BOX_INSET * 2
        graphics.roundedRectOutlined(boxX, boxY, boxRight, boxY + BOX_HEIGHT, GuiTheme.surface.rgba, GuiTheme.accent.rgba, 1f, GuiTheme.RADIUS)

        renderChildren(graphics, mouseX, mouseY)
    }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement, gson: Gson) {
        element.asString?.let { value = it }
    }

    private companion object {
        const val EXTRA_HEIGHT = 18
        const val BOX_HEIGHT = 20
        const val BOX_INSET = 6
        const val TEXT_INSET = 4
    }
}
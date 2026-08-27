package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.hoverTint
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.widget.isOver
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.capitalizeFirst
import com.odtheking.odin.utils.render.roundedOutline
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedRectOutlined
import com.odtheking.odin.utils.ui.animations.Easing
import com.odtheking.odin.utils.ui.animations.Fade
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent

val Enum<*>.label: String
    get() = toString().takeIf { it != name }
        ?: name.split('_').joinToString(" ") { it.lowercase().capitalizeFirst() }

class SelectorSetting<E : Enum<E>>(
    name: String,
    override val default: E,
    desc: String,
    val options: List<E> = default.declaringJavaClass.enumConstants.asList()
) : RenderableSetting<E>(name, desc), Saving {

    override var value: E = default

    private val expand = Fade(EXPAND_DURATION, Easing.EASE_IN_OUT)
    private val pillHover = Fade(HOVER_DURATION)

    override val clickButtons: IntArray = BOTH_BUTTONS

    fun cycle() {
        value = options[(options.indexOf(value) + 1) % options.size]
    }

    override fun measure() {
        height = expand.lerp(GuiTheme.ROW_HEIGHT, options.size * OPTION_HEIGHT + CLOSED_PADDING)
    }

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val label = value.label
        val pillWidth = mc.font.width(label) + PILL_PADDING * 2
        val pillX = x + width - RIGHT_PAD - pillWidth
        val pillY = y + (GuiTheme.ROW_HEIGHT - PILL_HEIGHT) / 2

        val over = isOver(mouseX, mouseY, pillX, pillY, pillWidth, PILL_HEIGHT)
        val fill = GuiTheme.surface.hoverTint(pillHover.progress(over), 1.2f)

        graphics.roundedRectOutlined(pillX, pillY, pillX + pillWidth, pillY + PILL_HEIGHT, fill, GuiTheme.accent.rgba, 1f, GuiTheme.RADIUS)

        drawLabel(graphics)
        graphics.text(mc.font, label, pillX + PILL_PADDING, GuiTheme.textY(pillY, PILL_HEIGHT), Colors.WHITE.rgba, false)

        renderExpanded(graphics) {
            val listX = x + LIST_INSET
            val listWidth = width - LIST_INSET * 2
            val listY = y + LIST_TOP
            graphics.roundedRect(
                listX, listY, listX + listWidth,
                listY + options.size * OPTION_HEIGHT, GuiTheme.surface.rgba, GuiTheme.RADIUS
            )

            options.forEachIndexed { index, option ->
                val optionY = listY + index * OPTION_HEIGHT
                if (index != options.lastIndex) {
                    graphics.fill(
                        listX + SEPARATOR_INSET, optionY + OPTION_HEIGHT,
                        listX + listWidth - SEPARATOR_INSET, optionY + OPTION_HEIGHT + 1, Colors.MINECRAFT_DARK_GRAY.rgba
                    )
                }
                graphics.centeredText(mc.font, option.label, x + width / 2, GuiTheme.textY(optionY, OPTION_HEIGHT), Colors.WHITE.rgba)
                if (isOver(mouseX, mouseY, listX, optionY, listWidth, OPTION_HEIGHT)) {
                    graphics.roundedOutline(
                        listX, optionY, listX + listWidth,
                        optionY + OPTION_HEIGHT + 1, GuiTheme.accent.rgba, 1.5f, GuiTheme.RADIUS
                    )
                }
            }
        }
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        val mouseY = event.y().toInt()

        if (mouseY < y + GuiTheme.ROW_HEIGHT) {
            if (event.button() == RIGHT) cycle() else expand.toggle()
            return
        }
        if (!expand.current || event.button() != LEFT) return

        options.getOrNull((mouseY - (y + LIST_TOP)) / OPTION_HEIGHT)?.let {
            value = it
            expand.progress(false)
        }
    }

    override fun release() {
        expand.progress(false)
    }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value.name)

    override fun read(element: JsonElement, gson: Gson) {
        val saved = element.asString ?: return
        value = options.firstOrNull { it.name == saved }
            ?: options.firstOrNull { it.label.equals(saved, ignoreCase = true) }
            ?: return
    }

    private companion object {
        const val EXPAND_DURATION = 200L
        const val HOVER_DURATION = 150L
        const val OPTION_HEIGHT = 16
        const val LIST_TOP = 22
        const val LIST_INSET = 4
        const val SEPARATOR_INSET = 10
        const val CLOSED_PADDING = 26
        const val PILL_HEIGHT = 17
        const val PILL_PADDING = 6
        const val RIGHT_PAD = 8
    }
}
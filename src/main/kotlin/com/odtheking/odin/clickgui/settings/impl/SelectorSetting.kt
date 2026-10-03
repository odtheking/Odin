package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.ui.*
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.capitalizeFirst
import com.odtheking.odin.utils.render.roundedOutline
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.ui.compose.*

val Enum<*>.label: String
    get() = toString().takeIf { it != name }
        ?: name.split('_').joinToString(" ") { it.lowercase().capitalizeFirst() }

class SelectorSetting<E : Enum<E>>(
    name: String,
    override val default: E,
    desc: String,
    val options: List<E> = default.declaringJavaClass.enumConstants.asList()
) : RenderableSetting<E>(name, desc), Saving {

    override var value: E by mutableStateOf(default)

    fun cycle() {
        value = options[(options.indexOf(value) + 1) % options.size]
    }

    @Composable
    override fun Content() {
        var expanded by remember { mutableStateOf(false) }
        OnDismiss { expanded = false }

        Column {
            SettingRow { Pill(value.label, onClick = { expanded = !expanded }, onRightClick = ::cycle) }
            AnimatedVisibility(expanded) {
                Column {
                    Gap(1)
                    Column {
                        options.forEachIndexed { index, option ->
                            Option(option, last = index == options.lastIndex) {
                                value = option
                                expanded = false
                            }
                        }
                    }
                        .width(LIST_WIDTH).offset({ LIST_INSET })
                        .drawBehind { graphics -> graphics.roundedRect(x, y, right, bottom, GuiTheme.surface.rgba, GuiTheme.RADIUS) }
                    Gap(LIST_INSET)
                }.width(GuiTheme.ROW_WIDTH)
            }
        }
    }

    @Composable
    private fun Option(option: E, last: Boolean, onClick: () -> Unit) = Box().size(LIST_WIDTH, OPTION_HEIGHT)
        .clickable(onClick = onClick).drawBehind { graphics ->
            if (!last) graphics.fill(x + SEPARATOR_INSET, bottom, right - SEPARATOR_INSET, bottom + 1, Colors.MINECRAFT_DARK_GRAY.rgba)
            if (Pointer.isOver(x, y, width, height)) graphics.roundedOutline(x, y, right, bottom + 1, GuiTheme.accent.rgba, 1.5f, GuiTheme.RADIUS)
            graphics.textCentered(option.label, x, y, right, bottom)
        }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value.name)

    override fun read(element: JsonElement, gson: Gson) {
        val saved = element.asString ?: return
        value = options.firstOrNull { it.name == saved }
            ?: options.firstOrNull { it.label.equals(saved, ignoreCase = true) }
            ?: return
    }

    private companion object {
        const val OPTION_HEIGHT = 16
        const val LIST_INSET = 4
        const val LIST_WIDTH = GuiTheme.ROW_WIDTH - LIST_INSET * 2
        const val SEPARATOR_INSET = 10
    }
}
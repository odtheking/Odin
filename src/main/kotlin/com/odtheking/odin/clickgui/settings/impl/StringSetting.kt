package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.ui.SettingRow
import com.odtheking.odin.clickgui.ui.TextBox
import com.odtheking.odin.utils.ui.compose.Column
import com.odtheking.odin.utils.ui.compose.offset
import com.odtheking.odin.utils.ui.compose.size

class StringSetting(
    name: String,
    override val default: String = "",
    val length: Int = 32,
    desc: String,
    private val placeholder: String
) : RenderableSetting<String>(name, desc), Saving {

    private var text by mutableStateOf(default)

    override var value: String
        get() = text
        set(value) {
            if (value.length <= length) text = value
        }

    @Composable
    override fun Content() {
        Column {
            SettingRow(height = LABEL_HEIGHT)
            TextBox(value, { value = it }, placeholder, length).size(GuiTheme.INNER_WIDTH, BOX_HEIGHT).offset({ GuiTheme.PADDING })
        }
    }

    override fun write(gson: Gson): JsonElement = JsonPrimitive(value)

    override fun read(element: JsonElement, gson: Gson) {
        element.asString?.let { value = it }
    }

    private companion object {
        const val LABEL_HEIGHT = 17
        const val BOX_HEIGHT = 20
    }
}
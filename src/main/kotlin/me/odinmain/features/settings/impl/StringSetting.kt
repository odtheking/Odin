package me.odinmain.features.settings.impl

import com.github.stivais.aurora.color.Color
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

/**
 * Setting that represents a string.
 *
 * @param default Default value for the setting.
 * @param length Maximum length of the string.
 * @param placeholder Placeholder string that appears inside the text input.
 */
class StringSetting(
    name: String,
    override val default: String = "",
    var length: Int = 20,
    private val placeholder: String = "",
    description: String,
) : Setting<String>(name, description), Saving {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    private var censors = false

    fun censors(): StringSetting {
        censors = true
        return this
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            value = it
        }
    }

    private fun getLengthColor(string: String) =
        if (string.length >= length) Color.RED else Color.RGB(200, 200, 200)
}
package me.odinmain.features.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting


/**
 * Setting that lets you pick between an array of strings.
 * @author Aton, Stivais
 */
class SelectorSetting(
    name: String,
    defaultSelected: String,
    var options: ArrayList<String>,
    desc: String,
    hidden: Boolean = false
) : Setting<Int>(name, hidden, desc), Saving {

    override val default: Int = optionIndex(defaultSelected)

    override var value: Int
        get() = index
        set(value) {
            index = value
        }

    var index: Int = optionIndex(defaultSelected)
        set(value) {
            field = if (value > options.size - 1) 0 else if (value < 0) options.size - 1 else value
        }

    var selected: String
        get() = options[index]
        set(value) {
            index = optionIndex(value)
        }

    override fun write(): JsonElement {
        return JsonPrimitive(selected)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            selected = it
        }
    }

    private fun optionIndex(string: String): Int {
        return options.map { it.lowercase() }.indexOf(string.lowercase()).coerceIn(0, options.size - 1)
    }
}
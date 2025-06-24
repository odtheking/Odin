package me.odinmain.features.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

/**
 * Setting that lets you pick between an array of strings.
 */
class SelectorSetting(
    name: String,
    default: String,
    var options: ArrayList<String>,
    description: String,
) : Setting<Int>(name, description), Saving {

    constructor(
        name: String,
        options: ArrayList<String>,
        default: String = options[0],
        description: String,
    ) : this(name, default, options, description)

    override val default: Int = optionIndex(default)

    override var value: Int
        get() = index
        set(value) {
            index = value
        }

    var index: Int = optionIndex(default)
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
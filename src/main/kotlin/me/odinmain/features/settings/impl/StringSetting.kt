package me.odinmain.features.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting


/**
 * Setting that lets you type a string.
 * @author Aton, Stivais
 */
class StringSetting(
    name: String,
    override val default: String = "",
    var length: Int = 20,
    desc: String,
    hidden: Boolean = false
) : Setting<String>(name, hidden, desc), Saving {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    var text: String by this::value

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            value = it
        }
    }
}
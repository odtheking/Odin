package me.odinmain.features.settings.impl

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import java.lang.reflect.Type

/**
 * This setting is only designed to store values as a map, and shouldn't be rendered in the gui.
 *
 * @author Stivais
 */
class MapSetting<K : Any?, V : Any?, T : MutableMap<K, V>>(
    name: String,
    override val default: T,
    private val type: Type,
) : Setting<T>(name, true, description = ""), Saving {

    override var value: T = default

    override fun write(): JsonElement {
        return gson.toJsonTree(value)
    }

    override fun read(element: JsonElement?) {
        element?.let {
            val temp = gson.fromJson<Map<K, V>>(it, type)
            value.clear()
            value.putAll(temp)
        }
    }
}

inline fun <reified K : Any?, reified V : Any?, reified T : MutableMap<K, V>> MapSetting(
    name: String,
    default: T,
): MapSetting<K, V, T> = MapSetting(name, default, object : TypeToken<T>() {}.type)
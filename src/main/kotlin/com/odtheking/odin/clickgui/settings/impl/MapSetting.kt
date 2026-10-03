package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.settings.Setting
import java.lang.reflect.Type

/**
 * This [Setting] is designed to store values within a [map][MutableMap].
 *
 * @param name Name of the setting (This is required to identify it while saving/loading)
 * @param default The map instance
 * @param type Type of the map (This is required due to kotlin erasing types, you can avoid this by using the Function)
 */
class MapSetting<K : Any, V : Any, T : MutableMap<K, V>>(
    name: String,
    override val default: T,
    private val type: Type,
) : Setting<T>(name, description = ""), Saving {

    override var value: T = default

    override fun write(gson: Gson): JsonElement = gson.toJsonTree(value)

    override fun read(element: JsonElement, gson: Gson) {
        val temp = gson.fromJson<Map<K, V>>(element, type)
        value.clear()
        value.putAll(temp)
    }
}

inline fun <reified K : Any, reified V : Any, reified T : MutableMap<K, V>> MapSetting(
    name: String,
    default: T,
): MapSetting<K, V, T> = MapSetting(name, default, object : TypeToken<T>() {}.type)
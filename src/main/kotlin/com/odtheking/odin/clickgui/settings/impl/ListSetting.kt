package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.settings.Setting
import java.lang.reflect.Type

/**
 * This setting is only designed to store values as a list.
 *
 * @author Stivais
 */
class ListSetting<E, T : MutableCollection<E>>(
    name: String,
    override val default: T,
    private val type: Type
) : Setting<T>(name, description = ""), Saving {

    override var value: T = default

    override fun write(gson: Gson): JsonElement = gson.toJsonTree(value)

    override fun read(element: JsonElement, gson: Gson) {
        element.asJsonArray?.let {
            val temp = gson.fromJson<T>(it, type)
            value.clear()
            value.addAll(temp)
        }
    }
}

inline fun <reified E : Any, reified T : MutableCollection<E>> ListSetting(
    name: String,
    default: T,
): ListSetting<E, T> = ListSetting(name, default, object : TypeToken<T>() {}.type)
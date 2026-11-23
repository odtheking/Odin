package com.odtheking.odin.clickgui.settings

import com.google.gson.Gson
import com.google.gson.JsonElement

/**
 * Used for settings that you want to save/load.
 */
interface Saving {
    /**
     * Used to update the setting from the json.
     */
    fun read(element: JsonElement, gson: Gson)

    /**
     * Used to create the json.
     */
    fun write(gson: Gson): JsonElement
}
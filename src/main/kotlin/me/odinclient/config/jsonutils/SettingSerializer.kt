package me.odinclient.config.jsonutils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import me.odinclient.features.settings.Setting
import me.odinclient.features.settings.impl.*
import java.lang.reflect.Type

class SettingSerializer : JsonSerializer<Setting<*>> {
    override fun serialize(src: Setting<*>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonObject().apply {
            when (src) {
                is BooleanSetting -> addProperty(src.name, src.enabled)
                is NumberSetting -> addProperty(src.name, src.valueAsDouble)
                is SelectorSetting -> addProperty(src.name, src.selected)
                is StringSetting -> addProperty(src.name, src.text)
                is ColorSetting -> addProperty(src.name, src.value.rgba)
                is DualSetting -> addProperty("${src.name}, ${src.left}, ${src.right}", if (src.enabled) "Right" else "Left")
            }
        }
    }
}
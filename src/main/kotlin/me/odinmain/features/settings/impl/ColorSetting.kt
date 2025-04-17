package me.odinmain.features.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.utils.render.Color

/**
 * Setting that represents a [Color].
 *
 * @author Stivais
 */
class ColorSetting(
    name: String,
    override val default: Color,
    var allowAlpha: Boolean = false,
    desc: String,
    hidden: Boolean = false
) : Setting<Color>(name, hidden, desc), Saving {

    override var value: Color = default.copy()

    var hue: Float
        get() = value.hue
        set(value) {
            this.value.hue = value.coerceIn(0f, 1f)
        }

    var saturation: Float
        get() = value.saturation
        set(value) {
            this.value.saturation = value.coerceIn(0f, 1f)
        }

    var brightness: Float
        get() = value.brightness
        set(value) {
            this.value.brightness = value.coerceIn(0f, 1f)
        }

    var alpha: Float
        get() = value.alphaFloat
        set(value) {
            this.value.alphaFloat = value.coerceIn(0f, 1f)
        }

    override fun read(element: JsonElement?) {
        if (element?.asString?.startsWith("#") == true) {
            value = Color(element.asString.drop(1))
        } else element?.asInt?.let {
            value = Color(it)
        }
    }

    override fun write(): JsonElement {
        return JsonPrimitive("#${this.value.hex}")
    }
}
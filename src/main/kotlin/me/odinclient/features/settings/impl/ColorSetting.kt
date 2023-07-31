package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting
import me.odinclient.utils.render.Color

class ColorSetting(
    name: String,
    override val default: Color,
    var allowAlpha: Boolean = false,
    hidden: Boolean = false,
    description: String? = null,
) : Setting<Color>(name, hidden, description){

    override var value: Color = default
        set(value) {
            field = processInput(value)
        }

    inline val rgb: Int
        get() = value.rgba

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
        get() = value.alpha
        set(value) {
            this.value.alpha = value.coerceIn(0f, 1f)
        }
}
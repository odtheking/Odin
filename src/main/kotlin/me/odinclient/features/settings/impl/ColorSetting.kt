package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting
import me.odinclient.features.settings.impl.ColorSetting.ColorComponent.*
import me.odinclient.utils.Utils.clamp
import java.awt.Color

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

    var red: Int
        get() = value.red
        set(input) {
            value = Color(input.clamp(0, 255), green, blue, alpha)
        }

    var green: Int
        get() = value.green
        set(input) {
            value = Color(red, input.clamp(0, 255), blue, alpha)
        }

    var blue: Int
        get() = value.blue
        set(input) {
            value = Color(red, green, input.clamp(0, 255), alpha)
        }

    var alpha: Int
        get() = value.alpha
        set (input) {
            value = Color(red, green, blue, input.clamp(0, 255).toInt())
        }

    fun getNumber(colorNumber: ColorComponent): Double {
        return when (colorNumber) {
            RED -> red.toDouble()
            GREEN -> green.toDouble()
            BLUE -> blue.toDouble()
            ALPHA -> alpha.toDouble()
        }
    }

    fun getNumber(index: Int): Double = getNumber(colors[index])

    fun setNumber(colorNumber: ColorComponent, number: Double) {
        when (colorNumber) {
            RED -> red = number.toInt()
            GREEN -> green = number.toInt()
            BLUE -> blue = number.toInt()
            ALPHA -> alpha = number.toInt()
        }
    }

    fun setNumber(index: Int, number: Double) = setNumber(colors[index], number)

    var colors = arrayOf(RED, GREEN, BLUE)

    init {
        if (allowAlpha) colors += ALPHA
    }

    enum class ColorComponent(
        val color: Color,
    ) {
        RED(Color(255, 0, 0)),
        GREEN(Color(0, 255, 0)),
        BLUE(Color(0, 100, 255)),
        ALPHA(Color(255, 255, 255));
    }
}
package me.odinclient.features.settings.impl

import cc.polyfrost.oneconfig.config.core.OneColor
import me.odinclient.features.settings.Setting
import net.minecraft.util.MathHelper
import java.awt.Color

class ColorSetting(
    name: String,
    override val default: OneColor,
    var allowAlpha: Boolean = false,
    hidden: Boolean = false,
    description: String? = null,
) : Setting<OneColor>(name, hidden, description){

    override var value: OneColor = default
        set(value) {
            field = processInput(value)
        }

    val javaColor: Color
        get() = value.toJavaColor()

    var red: Int
        get() = value.red
        set(input) {
            value = OneColor(MathHelper.clamp_int(input,0,255), green, blue, alpha)
        }
    var green: Int
        get() = value.green
        set(input) {
            value = OneColor(red, MathHelper.clamp_int(input,0,255), blue, alpha)
        }
    var blue: Int
        get() = value.blue
        set(input) {
            value = OneColor(red, green, MathHelper.clamp_int(input,0,255), alpha)
        }
    var alpha: Int
        get() = value.alpha
        set (input) {
            value = OneColor(red, green, blue, MathHelper.clamp_int(input,0,255))
        }

    fun getNumber(colorNumber: ColorComponent): Double {
        return when (colorNumber) {
            ColorComponent.RED -> red.toDouble()
            ColorComponent.GREEN -> green.toDouble()
            ColorComponent.BLUE -> blue.toDouble()
            ColorComponent.ALPHA -> alpha.toDouble()
        }
    }

    fun setNumber(colorNumber: ColorComponent, number: Double) {
        when (colorNumber) {
            ColorComponent.RED -> red = number.toInt()
            ColorComponent.GREEN -> green = number.toInt()
            ColorComponent.BLUE -> blue = number.toInt()
            ColorComponent.ALPHA -> alpha = number.toInt()
        }
    }

    /**
     * Returns an array of the available settings. Those are either red, green and blue or red, green, blue and alpha.
     */
    fun colors(): Array<ColorComponent> {
        val colors = arrayOf(ColorComponent.RED, ColorComponent.GREEN, ColorComponent.BLUE)
            if (allowAlpha) colors.plus(ColorComponent.ALPHA)
        return colors
    }

    enum class ColorComponent(
        val max: Double,
        val color: OneColor,
    ) {
        RED(255.0, OneColor(255, 0, 0)),
        GREEN(255.0, OneColor(0, 255, 0)),
        BLUE(255.0, OneColor(0, 100, 255)),
        ALPHA(255.0, OneColor(255, 255, 255));
    }
}
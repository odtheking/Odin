package me.odinmain.features.settings.impl


import me.odinmain.features.settings.Setting
import kotlin.math.round

/**
 * Setting that lets you pick a number between a range.
 * @author Stivais, Aton
 */
@Suppress("UNCHECKED_CAST")
class NumberSetting<E>(
    name: String,
    override val default: E = 1.0 as E, // hey it works
    min: Number = -10000,
    max: Number = 10000,
    increment: Number = 1,
    hidden: Boolean = false,
    description: String = "",
    val suffix: String = "",
) : Setting<E>(name, hidden, description) where E : Number, E : Comparable<E> {

    override var value: E = default
        set(value) {
            field = roundToIncrement(value).coerceIn(min, max) as E
        }

    override fun update(configSetting: Setting<*>) {
        valueDouble = (configSetting as NumberSetting).valueDouble
    }

    val increment = increment.toDouble()
    val min = min.toDouble()
    var max = max.toDouble()

    /**
     * Way for config to save due to errors.
     */
    var valueDouble
        get() = value.toDouble()
        set(value) {
            this.value = value as E
        }

    var valueInt
        get() = value.toInt()
        set(value) {
            this.value = value as E
        }

    private fun roundToIncrement(x: Number): Double {
        return round((x.toDouble() / increment)) * increment
    }
}

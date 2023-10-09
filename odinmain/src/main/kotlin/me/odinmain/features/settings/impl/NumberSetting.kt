package me.odinmain.features.settings.impl


import me.odinmain.features.settings.Setting
import me.odinmain.utils.Utils.coerceInNumber
import me.odinmain.utils.Utils.div
import kotlin.math.round

/**
 * Setting that lets you pick a number between a range.
 * @author Stivais, Aton
 */
@Suppress("UNCHECKED_CAST")
class NumberSetting<E>(
    name: String,
    override val default: E = 1.0 as E, // hey it works
    var min: Number = -10000,
    var max: Number = 10000,
    var increment: Number = 1,
    hidden: Boolean = false,
    description: String = "",
) : Setting<E>(name, hidden, description) where E : Number, E : Comparable<E> {

    override var value: E = default
        set (newVal) {
            field = roundToIncrement(processInput(newVal)).coerceInNumber(min, max) as E
        }

    /**
     * way for config to save due to errors.
     */
    var valueAsDouble
        get() = value.toDouble()
        set(value) {
            this.value = value as E
        }

    private fun roundToIncrement(x: Number): Number {
        return round((x / increment).toDouble()) * increment.toDouble()
    }
}

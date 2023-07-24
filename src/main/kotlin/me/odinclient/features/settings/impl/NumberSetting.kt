package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting
import me.odinclient.utils.Utils.coerceIn
import me.odinclient.utils.Utils.div
import kotlin.math.round

@Suppress("UNCHECKED_CAST")
class NumberSetting<E>(
    name: String,
    override val default: E = 1.0 as E, // hey it works
    var min: Number = -10000,
    var max: Number = 10000,
    var increment: Number = 1,
    hidden: Boolean = false,
    description: String? = null,
) : Setting<E>(name, hidden, description) where E : Number, E : Comparable<E> {

    override var value: E = default
        set (newVal) {
            field = roundToIncrement(processInput(newVal)).coerceIn(min, max) as E
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

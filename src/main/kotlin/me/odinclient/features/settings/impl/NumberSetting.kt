package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting
import me.odinclient.utils.Utils.clamp
import kotlin.math.round

@Suppress("UNCHECKED_CAST")
class NumberSetting<E>(
    name: String,
    override val default: E = 1.0 as E, // hey it works
    var min: Double = -10000.0,
    var max: Double = 10000.0,
    var increment: Double = 1.0,
    hidden: Boolean = false,
    description: String? = null,
) : Setting<E>(name, hidden, description) where E : Number, E : Comparable<E> {

    override var value: E = default
        set (newVal) {
            field = roundToIncrement(processInput(newVal).toDouble()).clamp(min, max) as E
        }

    /**
     * way to save and do maffs
     */
    var valueAsDouble
        get() = value.toDouble()
        set(value) {
            this.value = value as E
        }

    private fun roundToIncrement(x: Double): Double {
        return round(x / increment) * increment
    }
}
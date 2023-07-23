package me.odinclient.features.settings.impl

import me.odinclient.features.settings.Setting
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
            field = roundToIncrement(processInput(newVal).toDouble()).coerceIn(min, max) as E
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

fun Number.coerceIn(min: Number, max: Number): Number {
    return this.toDouble().coerceIn(min.toDouble(), max.toDouble())
}

operator fun Number.div(number: Number): Double {
    return this.toDouble() / number.toDouble()
}

operator fun Number.times(number: Number): Double {
    return this.toDouble() * number.toDouble()
}

operator fun Number.minus(number: Number): Double {
    return this.toDouble() - number.toDouble()
}

operator fun Number.plus(number: Number): Double {
    return this.toDouble() + number.toDouble()
}

operator fun Number.unaryMinus(): Number {
    return -this.toDouble()
}
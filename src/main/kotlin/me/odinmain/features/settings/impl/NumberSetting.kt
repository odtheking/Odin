package me.odinmain.features.settings.impl


import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
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
    desc: String,
    val unit: String = "",
    hidden: Boolean = false
) : Setting<E>(name, hidden, desc), Saving where E : Number, E : Comparable<E> {

    override var value: E = default
        set(value) {
            field = roundToIncrement(value).coerceIn(min, max) as E
        }

    /**
     * The amount a setting should increment.
     */
    val increment = increment.toDouble()

    /**
     * The minimum a setting can be.
     */
    val min = min.toDouble()

    /**
     * The maximum a setting can be.
     */
    var max = max.toDouble()

    /** Used for GUI Rendering as using [value] as Number is really inconvenient for maths */
    var valueDouble
        get() = value.toDouble()
        set(value) {
            this.value = value as E
        }

    /** Used for GUI Rendering as using [value] as Number is really inconvenient for maths */
    var valueInt
        get() = value.toInt()
        set(value) {
            this.value = value as E
        }

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asNumber?.let {
            value = it as E
        }
    }

    private fun roundToIncrement(x: Number): Double {
        return round((x.toDouble() / increment)) * increment
    }
}

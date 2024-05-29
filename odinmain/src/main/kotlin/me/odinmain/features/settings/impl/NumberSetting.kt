package me.odinmain.features.settings.impl


import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.slider
import com.github.stivais.ui.elements.scope.takeEvents
import com.github.stivais.ui.impl.ClickGUITheme
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.utils.floor
import me.odinmain.utils.round
import kotlin.math.round

/**
 * Setting that lets you pick a value between a range
 *
 * @param min The minimum a value can be
 * @param max The maximum a value can be
 * @param increment The increment for the setting
 * @param unit The suffix for value in the UI (It is recommended to set this for better UX)
 */
@Suppress("UNCHECKED_CAST")
class NumberSetting<E>(
    name: String,
    override val default: E = 1.0 as E,
    min: Number = -10000,
    max: Number = 10000,
    increment: Number = 1,
    hidden: Boolean = false,
    description: String = "",
    val unit: String = "",
) : Setting<E>(name, hidden, description), Saving where E : Number, E : Comparable<E> {

    override var value: E = default

    /** The amount a setting should increment. */
    val increment = increment.toDouble()

    /** The minimum a setting can be */
    val min = min.toDouble()

    /** The maximum a setting can be */
    var max = max.toDouble()

    private val text: String
        get() {
            val double = value.toDouble()
            val number = if (double - double.floor() == 0.0) value.toInt() else double.round(2)
            return "$number$unit"
        }

    override fun ElementScope<*>.createElement() {
        setting(44.px) {
            text(
                text = name,
                pos = at(6.px, 10.px),
                35.percent
            )
            val display = text(
                text = text,
                pos = at(x = -(6.px), y = 10.px),
                size = 35.percent
            )
            takeEvents(
                slider(
                    constraints = constrain(y = 75.percent, w = 95.percent, h = 18.percent),
                    color = ClickGUITheme,
                    value = value.toDouble(), min = min, max = max,
                    onChange = { percent ->
                        set(percent * (max - min) + min)
                        display.string = text
                    }
                )
            )
        }
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asNumber?.let {
            value = it as E
        }
    }

    fun set(new: Number) {
        value = (round((new.toDouble() / increment)) * increment).coerceIn(min, max) as E
    }
}

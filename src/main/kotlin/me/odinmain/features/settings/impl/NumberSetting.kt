@file:Suppress("always_false")

package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.impl.measurements.Animatable
import com.github.stivais.aurora.constraints.impl.measurements.Pixel
import com.github.stivais.aurora.constraints.impl.size.Copying
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.impl.Text.Companion.string
import com.github.stivais.aurora.utils.multiply
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 26`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.onValueChanged
import me.odinmain.features.settings.Setting.Renders.Companion.setting
import me.odinmain.utils.round
import kotlin.math.floor
import kotlin.math.round

/**
 * Setting that lets you pick a value between a range.
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
    description: String,
    val unit: String = "",
) : Setting<E>(name, hidden, description), Saving, Setting.Renders where E : Number, E : Comparable<E> {

    override var value: E = default

    /** The amount a setting should increment. */
    val increment = increment.toDouble()

    /** The minimum a setting can be */
    private val min = min.toDouble()

    /** The maximum a setting can be */
    private val max = max.toDouble()

    private val text: String
        get() {
            val double = value.toDouble()
            val number = if (double - floor(double) == 0.0) value.toInt() else double.round(2)
            return "$number$unit"
        }

    override fun ElementScope<*>.create() = setting(45.px) {

        var shouldAnimate = false
        val sliderWidth = Animatable.Raw(0f)

        onAdd {
            this@create.element.size()
            val to = getPercent() * element.width
            sliderWidth.to(to)
        }

        text(
            name,
            pos = at(x = Pixel.ZERO),
            size = 35.percent
        )
        text(
            string = text,
            pos = at(x = Pixel.ZERO.alignOpposite),
            size = 35.percent
        ) {
            onValueChanged {
                val to = getPercent() * this@create.element.width
                if (shouldAnimate || !element.pressed) {
                    shouldAnimate = false
                    sliderWidth.animate(to = to, 0.75.seconds, Animation.Style.EaseOutQuint)
                } else {
                    sliderWidth.to(to = to)
                }
                string = text
                redraw()
            }
        }

        block(
            constraints = constrain(y = 75.percent, w = Copying, h = 20.percent),
            color = `gray 26`,
            radius = 5.radius()
        ) {
            val color = Color.Animated(
                from = ClickGUI.color,
                to = Color.RGB(ClickGUI.color.rgba.multiply(1.2f))
            )
            block(
                constrain(0.px, 0.px, sliderWidth, Copying),
                color = color,
                radius = 5.radius()
            )
            onMouseEnterExit {
                color.animate(0.25.seconds, Animation.Style.Linear)
            }
            onClick {
                shouldAnimate = true
            }
            onMouseDrag { percent, _ ->
                set(percent * (max - min) + min)
                true
            }
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

    fun getPercent(): Float = ((value.toDouble() - min) / (max - min)).toFloat()
}

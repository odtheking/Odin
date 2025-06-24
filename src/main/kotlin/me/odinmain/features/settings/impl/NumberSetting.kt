@file:Suppress("always_false")

package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.components.impl.Layout
import com.github.stivais.aurora.components.impl.dropShadow
import com.github.stivais.aurora.components.impl.string
import com.github.stivais.aurora.components.scope.ContainerScope
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.input.Keys
import com.github.stivais.aurora.measurements.Measurement
import com.github.stivais.aurora.measurements.impl.Alignment
import com.github.stivais.aurora.measurements.impl.Center
import com.github.stivais.aurora.measurements.impl.Percent
import com.github.stivais.aurora.renderer.data.Radius.Companion.radius
import com.github.stivais.aurora.utils.Timing.Companion.seconds
import com.github.stivais.aurora.utils.withAlpha
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.aurora.utilities.AnimatedPercent
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.settings.RepresentableSetting
import me.odinmain.features.settings.Saving
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
    description: String,
    val unit: String = "",
) : RepresentableSetting<E>(name, description), Saving where E : Number, E : Comparable<E> {

    override var value: E = default

    /** The amount a setting should increment. */
    private val increment = increment.toDouble()

    /** The minimum a setting can be */
    private val min = min.toDouble()

    /** The maximum a setting can be */
    private val max = max.toDouble()

    private val valueString: String
        get() {
            val double = value.toDouble()
            val number = if (double - floor(double) == 0.0) value.toInt() else double.round(2)
            return "$number$unit"
        }

    override fun ContainerScope<*>.represent() = column(
        size = size(width = 100.percent),
        gap = Layout.Gap.Static(8.px)
    ) {
        val pointerPosition = AnimatedPercent(Percent(calculatePercent().coerceIn(.025f, .975f)))

        val size = animatable<Measurement.Size>(18.px, 22.px)

        var shouldAnimate = false
        var clicked = false

        row(
            size = size(width = 100.percent),
            gap = Layout.Gap.Auto,
            alignment = Layout.Alignment.Center
        ) {
            text(
                string = name,
                size = 18.px,
            )
            text(
                string = valueString,
                size = 18.px
            ) {
                onEvent(event = ValueChanged) {
                    val to = calculatePercent().coerceIn(.025f, .975f)
                    if (shouldAnimate || !clicked) {
                        pointerPosition.animate(0.5.seconds, Animation.Style.EaseOutQuint)
                        pointerPosition.to(to)
                        shouldAnimate = false
                    }
                    string = valueString
                    this@represent.component.redraw()
                }
            }
        }
        block(
            size = size(100.percent, 10.px),
            color = ClickGUI.gray38,
            strokeColor = ClickGUI.colorDarker,
            strokeWidth = 2.px,
            radius = 5.radius()
        ) {
            dropShadow(color = Color.BLACK.withAlpha(0.4f), blur = 5f, spread = 2f, offsetY = 2f)
            padding(1f, 1f) // for outline to not be covered

            block(
                position = at(0.px, 0.px),
                size = size(pointerPosition, 100.percent),
                color = ClickGUI.color,
                radius = 4.radius()
            )
            block(
                position = at(Alignment.Center(pointerPosition), Center),
                size = size(size, size),
                color = Color.WHITE,
                radius = 10.radius()
            ).dropShadow(color = Color.BLACK.withAlpha(0.3f), blur = 5f, spread = 2f)

            onMouseEnter {
                size.animate(0.3.seconds, Animation.Style.EaseOutQuint)
                this@represent.component.redraw()
            }
            onMouseExit {
                size.animate(0.3.seconds, Animation.Style.EaseOutQuint)
                this@represent.component.redraw()
            }

            onClick {
                clicked = true
                shouldAnimate = true

                val percent = ((aurora.inputManager.mouseX - component.x) / component.width).coerceIn(0f, 1f)
                set(percent * (max - min) + min)
                this@represent.component.redraw()

                true
            }
            onMouseMove {
                if (clicked) {
                    val percent = ((aurora.inputManager.mouseX - component.x) / component.width).coerceIn(0f, 1f)
                    set(percent * (max - min) + min)
                    pointerPosition.to(percent.coerceIn(.025f, .975f))
                    component.redraw()
                }
            }
            onRelease {
                if (clicked) this@represent.component.redraw()
                clicked = false
            }
            onBind(key = Keys.LEFT) {
                set(value.toDouble() - increment)
                true
            }
            onBind(key = Keys.RIGHT) {
                set(value.toDouble() + increment)
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

    private fun calculatePercent(): Float = ((value.toDouble() - min) / (max - min)).toFloat()
}

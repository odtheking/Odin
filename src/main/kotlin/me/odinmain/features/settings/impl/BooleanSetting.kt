package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.components.impl.Layout
import com.github.stivais.aurora.components.impl.dropShadow
import com.github.stivais.aurora.components.scope.ContainerScope
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.measurements.Measurement
import com.github.stivais.aurora.measurements.impl.Center
import com.github.stivais.aurora.renderer.data.Radius.Companion.radius
import com.github.stivais.aurora.utils.Timing.Companion.seconds
import com.github.stivais.aurora.utils.withAlpha
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.settings.RepresentableSetting
import me.odinmain.features.settings.Saving

/**
 * # BooleanSetting
 *
 * [Representable setting][RepresentableSetting], which stores a boolean value.
 */
class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    description: String,
) : RepresentableSetting<Boolean>(name, description), Saving {

    override var value: Boolean = default

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        if (element?.asBoolean != value) {
            value = !value
        }
    }

    override fun ContainerScope<*>.represent() = row(
        size = size(100.percent),
        gap = Layout.Gap.Auto,
        alignment = Layout.Alignment.Center
    ) {
        val color = Color.Animated(from = ClickGUI.gray38, ClickGUI.color)
        val circle = animatable<Measurement.Position>(from = 0.percent, to = 50.percent)

        if (value) {
            color.swap()
            circle.swap()
        }

        onEvent(event = ValueChanged) {
            circle.animate(0.4.seconds, Animation.Style.EaseInOutQuint)
            color.animate(0.4.seconds, Animation.Style.EaseInOutQuint)
        }

        text(
            string = name,
            size = 18.px
        )
        block(
            size = size(44.px, 24.px),
            color = color,
            strokeColor = ClickGUI.colorDarker,
            strokeWidth = 2.px,
            radius = 12.radius()
        ) {
            dropShadow(color = Color.BLACK.withAlpha(0.4f), blur = 5f, spread = 2f, offsetY = 2f)
            padding(4f, 4f)

            block(
                position = at(x = circle, y = Center),
                size = size(50.percent, 100.percent),
                color = Color.WHITE,
                radius = 10.radius()
            )
            onClick {
                value = !value
                true
            }
        }
    }

}
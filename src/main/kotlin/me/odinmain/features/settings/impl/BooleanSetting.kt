package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.impl.measurements.Animatable
import com.github.stivais.aurora.constraints.impl.measurements.Pixel
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import com.github.stivais.aurora.utils.color
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.onValueChanged
import me.odinmain.features.settings.Setting.Renders.Companion.setting

/**
 * A setting that represents a boolean.
 */
class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    hidden: Boolean = false,
    description: String,
): Setting<Boolean>(name, hidden, description), Saving, Setting.Renders {

    override var value: Boolean = default

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        if (element?.asBoolean != value) {
            value = !value
        }
    }

    override fun ElementScope<*>.create() = setting {
        text(
            name,
            pos = at(x = Pixel.ZERO),
            size = 40.percent
        )
        val pointer = Animatable(from = 30.percent, to = 70.percent, swapIf = value)
        val color = Color.Animated(from = `gray 38`, to = ClickGUI.color, swapIf = value)

        block(
            constrain(x = Pixel.ZERO.alignOpposite, w = 35.px, h = 50.percent),
            color,
            radius = 10.radius()
        ) {
            outline(color = color { ClickGUI.color.rgba }, thickness = 1.5.px)
            hoverEffect(factor = 1.25f)

            block(
                constrain(x = pointer.alignCenter, w = 50.percent, h = 80.percent),
                color = Color.WHITE,
                radius = 8.radius()
            )
            onClick {
                value = !value
            }
        }

        onValueChanged {
            color.animate(0.25.seconds, Animation.Style.EaseInOutQuint)
            pointer.animate(0.25.seconds, Animation.Style.EaseInOutQuint)
        }
    }
}
package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.*
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.elements.scope.*
import com.github.stivais.ui.utils.*
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

/**
 * A setting that represents a boolean.
 */
class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    hidden: Boolean = false,
    description: String = "",
): Setting<Boolean>(name, hidden, description), Saving {

    override var value: Boolean = default

    var enabled: Boolean by this::value

    override fun write(): JsonElement {
        return JsonPrimitive(enabled)
    }

    override fun read(element: JsonElement?) {
        if (element?.asBoolean != enabled) {
            enabled = !enabled
        }
    }

    override fun ElementScope<*>.createElement() {
        setting(40.px) {
            text(text = name, pos = at(x = 6.px), size = 40.percent)
            switch(
                constrain(x = -6.px, w = 35.px, h = 50.percent),
                color = Color.Animated(from = `gray 38`, to = ClickGUI.color),
                on = value
            ).onClick {
                value = !value
                true
            }
        }
    }

    fun ElementDSL.switch(
        constraints: Constraints? = null,
        color: Color.Animated,
        on: Boolean = false
    ): BlockScope {
        val color2 = color.color2
        val pointer = Animatable(from = 30.percent.center, to = 70.percent.center)
        if (on) {
            pointer.swap()
            color.swap()
        }
        return block(constraints, color, radius = 9.radii()) {
            outline(color { color2.rgba.darker() }, thickness = 1.5.px)
            hoverEffect()
            block(
                constrain(x = pointer, w = 50.percent, h = 80.percent),
                color = Color.WHITE,
                radius = 8.radii()
            )
            onClick {
                color.animate(0.25.seconds)
                pointer.animate(0.25.seconds, Animations.EaseInOutQuint)
                false
            }
        }
    }
}
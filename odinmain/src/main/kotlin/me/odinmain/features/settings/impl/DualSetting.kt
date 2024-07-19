package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.hoverEffect
import com.github.stivais.ui.utils.*
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

/**
 * A setting that represents a boolean, with a different style.
 *
 * @author Aton, Bonsai
 */
class DualSetting(
    name: String,
    val left: String,
    val right: String,
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

        val leftColor = Color.Animated(from = ClickGUI.color, to = `gray 38`)
        val rightColor = Color.Animated(from = `gray 38`, to = ClickGUI.color)

        if (value) {
            leftColor.swap()
            rightColor.swap()
        }

        setting(40.px) {
            block(
                constraints = constrain(x = 5.percent, w = 45.percent, h = 80.percent),
                color = leftColor,
                radius = radius(9f, 0f, 9f, 0f)
            ) {
                text(
                    text = left,
                    size = 60.percent
                )

                onClick {
                    leftColor.animate(0.25.seconds)
                    rightColor.animate(0.25.seconds)
                    value = !value
                    true
                }
                hoverEffect()
            }

            block(
                constraints = constrain(x = 50.percent, w = 45.percent, h = 80.percent),
                color = rightColor,
                radius = radius(0f, 9f, 0f, 9f)
            ) {
                text(
                    text = right,
                    size = 60.percent
                )

                onClick {
                    leftColor.animate(0.25.seconds)
                    rightColor.animate(0.25.seconds)
                    value = !value
                    true
                }
                hoverEffect()
            }
        }
    }
}
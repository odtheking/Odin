package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.impl.ClickGUITheme
import com.github.stivais.ui.impl.`gray 38`
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting


/**
 * Setting that lets you type a string.
 * @author Aton, Stivais
 */
// TODO:
// - add proper options for length, if should lock if width reached or continue
//
class StringSetting(
    name: String,
    override val default: String = "",
    var length: Int = 20,
    hidden: Boolean = false,
    description: String = "",
) : Setting<String>(name, hidden, description), Saving {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    var text: String by this::value

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            value = it
        }
    }

    override fun ElementScope<*>.createElement() {
        val thickness = Animatable(from = 1.px, to = 1.75.px)
        val hover = Color.Animated(from = `gray 38`, to = Color { `gray 38`.rgba.brighter(0.75) })
        setting(70.px).column(copies()) {
            divider(10.px)
            text(text = name, pos = at(6.px), size = 16.px)
            divider(5.px)
            block(
                constraints = size(95.percent, 30.px),
                color = hover,
                radius = 5.radii()
            ) {
                textInput(
                    text = value,
                    constraints = constrain(6.px, y = Center + 1.px, h = 50.percent),
                    maxWidth = 85.percent,
                    onTextChange = { str ->
                        value = if (str.length <= length) str else value
                    }
                ).apply {
                    onFocusGain { thickness.animate(0.25.seconds) }
                    onFocusLost { thickness.animate(0.25.seconds) }
                    onMouseEnterExit { // slight inconvenience
                        hover.animate(0.25.seconds)
                        true
                    }
                }
                onClick {
                    child(0)?.focusThis()
                    true
                }
                onMouseEnterExit {
                    hover.animate(0.25.seconds)
                    true
                }
                outline(ClickGUITheme, thickness)
                scissors()
            }
        }
    }
}
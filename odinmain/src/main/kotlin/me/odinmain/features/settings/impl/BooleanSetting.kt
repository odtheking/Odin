package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.color
import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.button
import com.github.stivais.ui.impl.ClickGUITheme
import com.github.stivais.ui.utils.radii
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
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
            text(
                text = name,
                pos = at(x = 6.px),
                size = 40.percent
            )
            button(
                constraints = constrain(x = -(6.px), w = 20.px, h = 50.percent),
                color = color(from = Color.RGB(38, 38, 38), to = ClickGUITheme),
                on = value,
                radii = radii(all = 5)
            ) {
                onClick(0) {
                    value = !value
                    true
                }
                outline(color = ClickGUITheme)
            }
        }
    }
}
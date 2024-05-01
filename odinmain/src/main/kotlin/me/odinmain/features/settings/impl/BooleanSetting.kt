package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.button
import com.github.stivais.ui.elements.text
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.testing.mainColor
import com.github.stivais.ui.utils.radii
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

/**
 * A setting that represents a boolean.
 *
 * @author Aton, Stivais
 */
class BooleanSetting (
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

    override fun getUIElement(parent: Element): SettingElement = parent.setting(40.px) {
        text(
            text = name,
            at = at(6.px, Center),
            size = 12.px
        )
        button(
            constraints = constrain(x = -6.px, y = Center, w = 20.px, h = 20.px),
            offColor = Color.RGB(38, 38, 38),
            onColor = mainColor,
            on = value,
            radii = radii(all = 5)
        ) {
            onClick(0) {
                value = !value
                true
            }
            outline(color = mainColor)
        }
    }
}
package me.odinmain.features.settings.impl

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.components.impl.Layout
import com.github.stivais.aurora.components.impl.dropShadow
import com.github.stivais.aurora.components.scope.ContainerScope
import com.github.stivais.aurora.dsl.at
import com.github.stivais.aurora.dsl.percent
import com.github.stivais.aurora.dsl.px
import com.github.stivais.aurora.dsl.size
import com.github.stivais.aurora.measurements.impl.Center
import com.github.stivais.aurora.renderer.data.Radius.Companion.radius
import com.github.stivais.aurora.utils.*
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.aurora.components.popup
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.settings.RepresentableSetting
import me.odinmain.features.settings.Saving

class ColorSetting(
    name: String,
    color: Color,
    val allowAlpha: Boolean = true,
    description: String,
) : RepresentableSetting<Color.HSB>(name, description), Saving {

    override val default: Color.HSB = color.toHSB()

    override var value: Color.HSB = default

    override fun ContainerScope<*>.represent() = row(
        size = size(width = 100.percent),
        gap = Layout.Gap.Auto,
        alignment = Layout.Alignment.Center
    ) {
        text(
            string = name,
            size = 18.px
        )
        block(
            size = size(width = 44.px, 25.px),
            color = ClickGUI.gray38,
            strokeColor = color {
                value.rgba.withAlpha(1f)
            },
            strokeWidth = 2.px,
            radius = 8.radius()
        ) {
            dropShadow(color = Color.BLACK.withAlpha(0.4f), blur = 5f, spread = 2f, offsetY = 2f)
            padding(horizontal = 3f, vertical = 3f)

            block(
                size = size(100.percent, 100.percent),
                color = value,
                radius = 5.radius()
            )

            onClick {
                popup(
                    position = at(Center, Center),
                    size = size(100.px, 100.px),
                ) {
                    block(size = size(100.px, 100.px), color = Color.RED) {
                        onClick {
                            this@popup.component.parent?.removeComponent(this@popup.component)
                            true
                        }
                    }
                }
                true
            }
        }
    }

    override fun read(element: JsonElement?) {
        if (element?.asString?.startsWith("#") == true) {
            value = Color.RGB(hexToRGBA(element.asString)).toHSB()
        } else {
            element?.asInt?.let { value = Color.RGB(it).toHSB() }
        }
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value.toHexString(allowAlpha))
    }
}

package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.impl.measurements.Animatable
import com.github.stivais.aurora.constraints.impl.measurements.Pixel
import com.github.stivais.aurora.constraints.impl.size.Copying
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.Layout.Companion.section
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import com.github.stivais.aurora.elements.impl.Text.Companion.string
import com.github.stivais.aurora.elements.impl.TextInput.Companion.maxWidth
import com.github.stivais.aurora.elements.impl.TextInput.Companion.onTextChanged
import com.github.stivais.aurora.utils.multiply
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.setting

/**
 * Setting that represents a string.
 *
 * @param default Default value for the setting.
 * @param length Maximum length of the string.
 * @param placeholder Placeholder string that appears inside the text input.
 */
class StringSetting(
    name: String,
    override val default: String = "",
    var length: Int = 20,
    private val placeholder: String = "",
    hidden: Boolean = false,
    description: String,
) : Setting<String>(name, hidden, description), Saving, Setting.Renders {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    private var censors = false

    fun censors(): StringSetting {
        censors = true
        return this
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            value = it
        }
    }

    override fun ElementScope<*>.create() = setting(60.px) {

        val thickness = Animatable(1.px, 1.5.px)
        val hoverColor = Color.Animated(from = `gray 38`, to = Color.RGB(`gray 38`.rgba.multiply(1.2f)))

        column(size(w = Copying), padding = 2.5.px) {
            section(size = 20.px) {
                text(
                    name,
                    pos = at(x = Pixel.ZERO),
                    size = 80.percent
                )
            }

            block(
                size(w = Copying, h = 30.px),
                color = hoverColor,
                radius = 5.radius()
            ) {
                outline(
                    ClickGUI.color,
                    thickness = 1.px
                )

                val lengthText = text(
                    "${value.length}/$length",
                    pos = at(x = 3.percent.alignOpposite),
                    color = getLengthColor(value),
                    size = 40.percent,
                ).toggle()

                val input = textInput(
                    value,
                    placeholder,
                    pos = at(x = 3.percent),
                    size = 55.percent,
                ) {
                    // doesn't animate, but used for just swapping
                    val maxWidth = Animatable(from = 90.percent, to = 75.percent)
                    maxWidth(maxWidth)

                    onTextChanged { event ->
                        var str = event.string
                        if (str.length > length) str = str.substring(0, length)
                        lengthText.string = "${str.length}/$length"
                        lengthText.element.color = getLengthColor(str)
                        event.string = str
                        value = str
                    }
                    onFocusChanged {
                        thickness.animate(0.25.seconds, style = Animation.Style.EaseInOutQuint)
                        lengthText.toggle()
                        maxWidth.swap()
                        this@setting.redraw()
                    }
                }

                onClick {
                    ui.focus(input.element)
                }

                onMouseEnterExit {
                    hoverColor.animate(0.25.seconds, style = Animation.Style.EaseOutQuad)
                }
            }
        }
    }

    private fun getLengthColor(string: String) =
        if (string.length >= length) Color.RED else Color.RGB(200, 200, 200)
}
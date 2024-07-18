package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.TextInput
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.renderer.Image
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

/**
 * Setting that lets you type a string.
 * @author Aton, Stivais
 */
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

    override fun ElementScope<*>.createElement() {
        val thickness = Animatable(from = 1.px, to = 1.75.px)
        val hover = Color.Animated(from = `gray 38`, to = `gray 38 brighter`)
        setting(70.px) {
            text(
                text = name,
                pos = at(x = 6.px, y = 10.px),
                size = 16.px
            )
            block(
                constraints = constrain(y = 30.px, w = 95.percent, h = 30.px),
                color = hover,
                radius = 5.radii()
            ) {
                val maxWidth = if (censors) 80.percent else 95.percent
                val input = textInput(
                    text = value,
                    constraints = at(6.px, y = Center + 1.px), // todo: fix text offset
                    size = 50.percent,
                    maxWidth = maxWidth,
                    censored = censors,
                    onTextChange = { str -> // todo: change this to an event and allow for cancelling
                        value = if (str.length <= length) str else value
                    }
                ).apply {
                    onFocusGain { thickness.animate(0.25.seconds) }
                    onFocusLost { thickness.animate(0.25.seconds) }
                }
                onClick {
                    input.focusThis(); true
                }
                onMouseEnterExit {
                    hover.animate(0.25.seconds); false
                }
                outline(ClickGUI.color, thickness)
                scissors()

                // testing
                if (censors) {
                    image(
                        Image("/assets/odinmain/clickgui/visibility-show.svg", type = Image.Type.VECTOR),
                        constraints = constrain(-6.px, w = Test(), h = 75.percent)
                    ) {
                        onClick {
                            (input.element as TextInput).censorInput = !(input.element).censorInput
                            if (input.element.censorInput) {
                                element.image = Image("/assets/odinmain/clickgui/visibility-show.svg")
                            } else {
                                element.image = Image("/assets/odinmain/clickgui/visibility-off.svg")
                            }
                            true
                        }
                    }
                }
            }
        }
    }

    class Test : Size {
        override fun get(element: Element, type: Type): Float {
            return (if (type.axis == Constraint.HORIZONTAL) element.height else element.width)
        }
    }

    private companion object {
        private val `gray 38 brighter` = Color { `gray 38`.rgba.brighter(1.2) }
    }
}
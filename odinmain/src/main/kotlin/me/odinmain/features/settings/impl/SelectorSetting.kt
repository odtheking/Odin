package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.impl.TextScope
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.impl.ClickGUITheme
import com.github.stivais.ui.impl.`gray 38`
import com.github.stivais.ui.utils.*
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting


/**
 * Setting that lets you pick between an array of strings.
 */
class SelectorSetting(
    name: String,
    default: String,
    var options: ArrayList<String>,
    hidden: Boolean = false,
    description: String = "",
) : Setting<Int>(name, hidden, description), Saving {

    constructor(
        name: String,
        options: ArrayList<String>,
        default: String = options[0],
        hidden: Boolean = false,
        description: String = "",
    ) : this(name, default, options, hidden, description)

    override val default: Int = optionIndex(default)

    override var value: Int
        get() = index
        set(value) {
            index = value
        }

    var index: Int = optionIndex(default)
        set(value) {
            field = if (value > options.size - 1) 0 else if (value < 0) options.size - 1 else value
        }

    var selected: String
        get() = options[index]
        set(value) {
            index = optionIndex(value)
        }

    override fun write(): JsonElement {
        return JsonPrimitive(selected)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            selected = it
        }
    }

    private fun optionIndex(string: String): Int {
        return options.map { it.lowercase() }.indexOf(string.lowercase()).coerceIn(0, options.size - 1)
    }

    override fun ElementScope<*>.createElement() {

        var text: TextScope? = null
        // temp/test
        val alphaAnim = Animatable(0.25.px, 1.px)

        val height = Animatable(from = 40.px, to = (50 + 32 * options.size).px)
        val thickness = Animatable(from = 1.px, to = 1.75.px)

        setting(height) {
            column(copies()) {
                group(constraints = size(w = Copying, h = 40.px)) {
                    text(
                        text = name,
                        pos = at(x = 6.px),
                        size = 40.percent
                    )
                    block(
                        constraints = constrain(x = -6.px, w = Bounding + 6.px, h = 72.5.percent),
                        color = `gray 38`,
                        radius = radii(5)
                    ) {
                        outline(
                            color = ClickGUITheme,
                            thickness
                        )
                        text = text(
                            text = options[value]
                        )
                        onClick {
                            alphaAnim.animate(0.25.seconds, Animations.EaseInOutQuint)
                            height.animate(0.25.seconds, Animations.EaseInOutQuint)
                            thickness.animate(0.25.seconds, Animations.EaseInOutQuint)
                            this@setting.redraw()
                            true
                        }
                    }
                }
                divider(5.px)
                column(size(w = 95.percent, h = Bounding)) {
                    element.alphaAnim = alphaAnim
                    // background
                    block(
                        constraints = copies(),
                        color = `gray 38`,
                        radius = radii(all = 5)
                    ).outline(color = ClickGUITheme)

                    // options
                    // they're transparent, except for the outline which is animated on hover
                    for ((index, option) in options.withIndex()) {
                        block(
                            constraints = size(w = Copying, h = 32.px),
                            color = color(from = Color.TRANSPARENT, to = Color.RGB(150, 150, 150, 0.2f)),
                            radius = radii(5)
                        ) {
                            onClick {
                                text!!.string = option
                                value = index
                                alphaAnim.animate(0.25.seconds, Animations.EaseInOutQuint)
                                height.animate(0.25.seconds, Animations.EaseInOutQuint)
                                thickness.animate(0.25.seconds, Animations.EaseInOutQuint)
                                this@setting.redraw()
                                true
                            }
                            onMouseEnterExit {
                                color!!.animate(duration = 0.05.seconds)
                                true
                            }
                            text(text = option)
                        }
                    }
                }
            }
        }
    }
}
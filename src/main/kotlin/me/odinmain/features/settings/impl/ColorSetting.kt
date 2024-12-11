package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.impl.measurements.Animatable
import com.github.stivais.aurora.constraints.impl.measurements.Pixel
import com.github.stivais.aurora.constraints.impl.positions.Center
import com.github.stivais.aurora.constraints.impl.size.AspectRatio
import com.github.stivais.aurora.constraints.impl.size.Bounding
import com.github.stivais.aurora.constraints.impl.size.Fill
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.Layout.Companion.section
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import com.github.stivais.aurora.elements.impl.Popup
import com.github.stivais.aurora.elements.impl.Shadow
import com.github.stivais.aurora.elements.impl.Text.Companion.string
import com.github.stivais.aurora.elements.impl.TextInput.Companion.onTextChanged
import com.github.stivais.aurora.elements.impl.layout.Column.Companion.sectionRow
import com.github.stivais.aurora.elements.impl.popup
import com.github.stivais.aurora.renderer.data.Gradient
import com.github.stivais.aurora.transforms.impl.Scale
import com.github.stivais.aurora.utils.*
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI.favoriteColors
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.onValueChanged
import me.odinmain.features.settings.Setting.Renders.Companion.setting
import me.odinmain.utils.ui.image
import java.awt.Color.HSBtoRGB

class ColorSetting(
    name: String,
    color: Color,
    val allowAlpha: Boolean = true,
    description: String,
    hidden: Boolean = false
) : Setting<Color.HSB>(name, hidden, description), Saving, Setting.Renders {

    override val default: Color.HSB = color.toHSB()

    override var value: Color.HSB = default

    /**
     * Reference for the color picker popup.
     */
    private var popup: Popup? = null

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

    override fun ElementScope<*>.create() = setting(40.px) {
        text(
            name,
            pos = at(x = Pixel.ZERO),
            size = 40.percent
        )
        outlineBlock(
            constrain(x = Pixel.ZERO.alignOpposite, w = 30.px, h = 50.percent),
            color { value.withAlpha(255).rgba },
            thickness = 1.px,
            radius = 5.radius()
        ) {
            block(
                constraints = indent(2f),
                color = value,
                radius = 4.radius()
            )
            onClick {
                popup?.closePopup()
                popup = colorPicker()
                true
            }
            onValueChanged { event ->
                popup?.let {
                    passEvent(event, it)
                }
            }
        }
    }

    private fun ElementScope<*>.colorPicker() = popup(constraints = copies(), smooth = true) {
        val colorMaxBrightness = color { HSBtoRGB(value.hue, value.saturation, 1f) }
        val colorOnlyHue = color { HSBtoRGB(value.hue, 1f, 1f) }

        onClick {
            closePopup()
            popup = null
        }

        block(
            size(Bounding + 12.px, Bounding + 12.px),
            color = Color.RGB(22, 22, 22),
            radius = 10.radius()
        ) {
            draggable()

            // consume event so it doesn't pass to background
            // when hovering of any part of the color picker.
            onClick { true }

            Shadow(
                copies(),
                blur = 5f,
                spread = 5f,
                radii = 10.radius(),
            ).add()

            outline(
                colorMaxBrightness,
                thickness = 1.px,
            )

            column(padding = 10.px) {
                section(20.px) {
                    text(
                        "Color",
                        pos = at(x = Pixel.ZERO),
                        size = 75.percent
                    )

                    image(
                        "clickgui/close_icon.svg".image(),
                        constrain(x = Pixel.ZERO.alignOpposite, w = AspectRatio(1f), h = 80.percent)
                    ) {
                        onClick {
                            closePopup()
                            popup = null
                            true
                        }
                    }
                }

                row(padding = 7.5.px) {
                    /**
                     * Saturation and brightness slider
                     */
                    outlineBlock(
                        size(w = 200.px, h = 200.px),
                        colorMaxBrightness,
                        thickness = 1.px,
                        radius = 7.5.radius()
                    ) {
                        block(
                            indent(amount = 2f),
                            colors = Color.WHITE to colorOnlyHue,
                            gradient = Gradient.LeftToRight,
                            radius = 6.radius()
                        )
                        block(
                            indent(amount = 2f),
                            colors = Color.TRANSPARENT to Color.BLACK,
                            gradient = Gradient.TopToBottom,
                            radius = 6.radius()
                        )

                        pointer(onlyY = false) {
                            value.saturation to 1f - value.brightness
                        }
                        onMouseDrag { x, y ->
                            value.saturation = x
                            value.brightness = 1f - y
                            true
                        }
                    }

                    /**
                     * Hue slider
                     */
                    outlineBlock(
                        size(w = 15.px, h = 200.px),
                        colorMaxBrightness,
                        thickness = 1.px,
                        radius = 7.5.radius()
                    ) {
                        image(
                            "clickgui/HueGradient.png".image(),
                            constraints = indent(amount = 2f),
                            radius = 5.radius()
                        )
                        pointer(onlyY = true) {
                            0f to 1f - value.hue
                        }
                        onMouseDrag { _, y ->
                            value.hue = 1f - y
                            true
                        }
                    }

                    /**
                     * Alpha slider
                     */
                    if (allowAlpha) {
                        outlineBlock(
                            size(w = 15.px, h = 200.px),
                            colorMaxBrightness,
                            thickness = 1.px,
                            radius = 7.5.radius()
                        ) {
                            block(
                                constraints = indent(2f),
                                colors = colorOnlyHue to Color.TRANSPARENT,
                                gradient = Gradient.TopToBottom,
                                radius = 5.radius(),
                            )

                            pointer(onlyY = true) {
                                0f to 1f - value.alpha
                            }
                            onMouseDrag { _, y ->
                                value.alpha = 1f - y
                                true
                            }
                        }
                    }
                }

                /**
                 * Hex input
                 */
                sectionRow(size = 25.px, padding = 10.px) {
                    text(
                        "Hex",
                        pos = at(y = Center),
                        size = 65.percent,
                    )
                    block(
                        size(w = Fill, h = 90.percent),
                        color = `gray 38`,
                        radius = 6.radius()
                    ) {
                        outline(
                            colorMaxBrightness,
                            thickness = 1.px,
                        )
                        val textInput = textInput(
                            string = value.toHexString(allowAlpha),
                            placeholder = if (allowAlpha) "#FFFFFFFF" else "#FFFFFF",
                            pos = at(x = 5.percent),
                            size = 55.percent
                        ) {
                            onTextChanged { event ->
                                val str = event.string
                                val hexLength = if (allowAlpha) 9 else 7
                                // Validate hex input
                                if (str.length > hexLength || (str.isNotEmpty() && !str.startsWith("#")) || (str.length > 1 && !str.substring(1).all { it.isDigit() || it.lowercaseChar() in 'a'..'f' })) {
                                    event.cancel()
                                } else if (str.length == hexLength) {
                                    try {
                                        // Parse and update color from hex immediately
                                        val newColor = Color.RGB(hexToRGBA(str)).toHSB()
                                        value.hue = newColor.hue
                                        value.saturation = newColor.saturation
                                        value.brightness = newColor.brightness
                                        if (allowAlpha) value.alpha = newColor.alpha
                                    } catch (_: Exception) { }
                                }
                            }
                        }
                        onValueChanged {
                            // change hex if value changed externally
                            textInput.string = value.toHexString(allowAlpha)
                        }
                        onFocusLost {
                            // reset hex if invalid
                            val hexLength = if (allowAlpha) 9 else 7
                            if (textInput.string.length != hexLength) {
                                textInput.string = value.toHexString(allowAlpha)
                            }
                        }
                        onClick {
                            ui.focus(textInput.element)
                        }
                    }
                }

                /**
                 * Favourite colors.
                 */
                sectionRow(padding = 3.percent) {
                    block(
                        size(w = 14.percent, h = AspectRatio(1f)),
                        color = Color.RGB(22, 22, 22),
                        radius = 6.radius()
                    ) {
                        outline(
                            colorMaxBrightness,
                            thickness = 1.px,
                        )

                        var clicked = false
                        val transform = Scale.Animated(from = 1f, to = 0.9f)

                        image(
                            "clickgui/heart_icon.svg".image(),
                            constraints = size(70.percent, 70.percent),
                        ).transform(transform)

                        onClick {
                            if (!favoriteColors.contains(value)) {
                                if (favoriteColors.size == 5) favoriteColors.removeLast()
                                favoriteColors.add(0, Color.HSB(value))
                            }
                            clicked = true
                            transform.animate(0.1.seconds, Animation.Style.EaseInQuint)
                            true
                        }
                        onRelease {
                            if (clicked) {
                                transform.animate(0.15.seconds, Animation.Style.EaseInQuint)
                                clicked = false
                            }
                        }
                    }

                    repeat(5) { index ->
                        block(
                            size(w = 14.percent, h = AspectRatio(1f)),
                            color = Color.RGB(22, 22, 22),
                            radius = 6.radius()
                        ) {
                            outline(
                                color { (favoriteColors.getOrNull(index) ?: `gray 38`).rgba },
                                thickness = 1.px
                            )
                            block(
                                indent(2f),
                                color { (favoriteColors.getOrNull(index) ?: Color.TRANSPARENT).rgba },
                                radius = 5.radius()
                            )
                            onClick {
                                val favoriteColor = favoriteColors.getOrNull(index)
                                if (favoriteColor != null) {
                                    value.hue = favoriteColor.hue
                                    value.saturation = favoriteColor.saturation
                                    value.brightness = favoriteColor.brightness
                                    if (allowAlpha) value.alpha = favoriteColor.alpha
                                }
                                true
                            }
                            onClick(button = 1) {
                                if (favoriteColors.size - 1 >= index) {
                                    favoriteColors.removeAt(index)
                                }
                                true
                            }
                        }
                    }
                }
            }
        }
    }

    private inline fun ElementScope<*>.pointer(
        onlyY: Boolean,
        crossinline block: () -> Pair<Float, Float>,
    ) {
        val (sx, sy) = block()
        val pointerX = if (onlyY) null else Animatable.Raw((sx * 200f).coerceIn(8f, 192f))
        val pointerY = Animatable.Raw((sy * 200f).coerceIn(8f, 192f))

        var animate = false

        block(
            constrain(
                x = pointerX?.alignCenter ?: Center,
                y = pointerY.alignCenter,
                w = 10.px, h = 10.px
            ),
            color = value,
            radius = 5.radius()
        ).outline(color = Color.WHITE, thickness = 1.px)

        onClick {
            animate = true
        }
        onValueChanged {
            val duration = if (animate || !ui.eventManager.mouseDown) 0.15.seconds else 0f
            val (x, y) = block()
            pointerX?.animate(to = (x * 200f).coerceIn(8f, 192f), duration, Animation.Style.EaseOutQuad)
            pointerY.animate(to = (y * 200f).coerceIn(8f, 192f), duration, Animation.Style.EaseOutQuad)
            animate = false
        }
    }
}

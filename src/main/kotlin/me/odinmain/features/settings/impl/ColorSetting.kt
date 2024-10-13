package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.*
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Aspect
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.slider
import com.github.stivais.ui.renderer.Gradient.LeftToRight
import com.github.stivais.ui.renderer.Gradient.TopToBottom
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.utils.skyblock.modMessage
import java.awt.Color.HSBtoRGB

class ColorSetting(
    name: String,
    color: Color,
    val allowAlpha: Boolean = true,
    description: String = "",
    hidden: Boolean = false
) : Setting<Color.HSB>(name, hidden, description), Saving {

    override val default: Color.HSB = color.toHSB()

    override var value: Color.HSB = default

    override fun read(element: JsonElement?) {
        if (element?.asString?.startsWith("#") == true) {
            value = colorFrom(element.asString).toHSB()
        } else {
            element?.asInt?.let { value = Color.RGB(it).toHSB() }
        }
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value.toHexString())
    }

    override fun ElementScope<*>.createElement() {
        val size = Animatable(from = 40.px, to = Bounding)
        val alpha = Animatable(0.px, 1.px)

        val hueMax = color { HSBtoRGB(value.hue, 1f, 1f) }

        setting(size) {
            group(constrain(0.px, 0.px, w = Copying, h = 40.px)) {
                text(
                    text = name,
                    pos = at(x = 6.px),
                    size = 40.percent
                )
                // color preview + dropdown button thingy
                block(
                    constraints = constrain(x = -(6.px), w = 30.px, h = 50.percent),
                    color = transparentFix,
                    radius = 5.radii()
                ) {
                    outline(color = color { value.withAlpha(255).rgba })
                    block(
                        constraints = indent(2),
                        color = value,
                        radius = 4.radii()
                    )
                    onClick {
                        size.animate(0.25.seconds, Animations.EaseInOutQuint)
                        alpha.animate(0.25.seconds, Animations.Linear)
                        this@setting.redraw()
                        true
                    }
                }
            }

            column(constraints = constrain(0.px, 40.px, w = Copying)) {
                element.alphaAnim = alpha
                text("wip, need popup color picker", size = 10.px)
                saturationAndBrightness(hueMax)
                divider(10.px)
                hueSlider()
                if (allowAlpha) {
                    modMessage("a")
                    divider(10.px)
                    alphaSlider(hueMax)
                }
            }
        }
    }

    private fun ElementDSL.saturationAndBrightness(hueMax: Color) {
        val x = Animatable.Raw((228f * value.saturation).coerceIn(8f, 220f))
        val y = Animatable.Raw((170f * (1f - value.brightness)).coerceIn(8f, 220f))
        block(
            size(w = 95.percent, h = Aspect(228f / 170f)),
            colors = Color.WHITE to hueMax,
            radius = 5.radii(),
            gradient = LeftToRight
        ) {
            block(
                constraints = copies(),
                // temp fix until I figure out why nanovg doesn't render anything under 0.2f alpha
                colors = transparentFix to Color.BLACK,
                radius = 5.radii(),
                gradient = TopToBottom
            ) {
                block(
                    constraints = constrain(x.center, y.center, w = 10.px, h = 10.px),
                    color = value,
                    radius = 5.radii()
                ).outline(Color.WHITE)
            }
            slider(
                accepts = true,
                onChange = { px, py, isClick ->
                    val toX = (px * element.width).coerceIn(8f, element.width - 8f)
                    val toY = (py * element.height).coerceIn(8f, element.height - 8f)
                    if (isClick) {
                        x.animate(to = toX, 0.1.seconds, Animations.EaseOutQuad)
                        y.animate(to = toY, 0.1.seconds, Animations.EaseOutQuad)
                    } else {
                        x.to(toX)
                        y.to(toY)
                        redraw()
                    }
                    value.saturation = px
                    value.brightness = 1f - py
                }
            )
        }
    }

    private fun ElementDSL.hueSlider() {
        val x = Animatable.Raw((228f * value.hue).coerceIn(8f, 220f))
        image(
            "/assets/odinmain/clickgui/HueGradient.png",
            size(w = 95.percent, h = 15.px),
            radius = 5.radii()
        ) {
            block(
                constraints = constrain(x.center, w = 10.px, h = 10.px),
                color = value,
                radius = 5.radii()
            ).outline(Color.WHITE)
            slider(
                accepts = true,
                onChange = { px, _, isClick ->
                    val to = (px * element.width).coerceIn(8f, 220f)
                    if (isClick) x.animate(to = to, 0.75.seconds, Animations.EaseOutQuint) else x.to(to)
                    value.hue = px
                    redraw()
                }
            )
        }
    }

    private fun ElementDSL.alphaSlider(hueMax: Color) {
        val x = Animatable.Raw((228f * value.alpha).coerceIn(8f, 220f))
        block(
            size(w = 95.percent, h = 15.px),
            colors = transparentFix to hueMax,
            radius = 5.radii(),
            gradient = LeftToRight
        ) {
            block(
                constraints = constrain(x.center, w = 10.px, h = 10.px),
                color = value,
                radius = 5.radii()
            ).outline(Color.WHITE)
            slider(
                accepts = true,
                onChange = { px, _, isClick ->
                    val to = (px * element.width).coerceIn(8f, 220f)
                    if (isClick) x.animate(to = to, 0.75.seconds, Animations.EaseOutQuint) else x.to(to)
                    value.alpha = px
                    redraw()
                }
            )
        }
    }

    private companion object {
        @JvmStatic
        val transparentFix: Color.RGB = Color.RGB(0, 0, 0, 0.2f)
    }
}
